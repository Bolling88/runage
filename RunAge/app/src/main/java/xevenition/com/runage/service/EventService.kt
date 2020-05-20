package xevenition.com.runage.service

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.*
import xevenition.com.runage.service.ActivityBroadcastReceiver.Companion.KEY_EVENT_BROADCAST_ID
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.*
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class EventService : Service() {

    private var textToSpeech: TextToSpeech? = null

    // private var wakeLock: PowerManager.WakeLock? = null
    private var activityType: Int = DetectedActivity.STILL
    private lateinit var currentQuest: Quest
    private var countdownFinished = false
    private var previousLocation: Location? = null
    private lateinit var eventHandler: ActivityActivator
    private val compositeDisposable = CompositeDisposable()
    private var nextDistanceFeedback = 1
    private lateinit var locationCallback: LocationCallback
    private val binder = LocalBinder()
    private var callback: EventCallback? = null
    private var isMetric: Boolean = false

    interface EventCallback {
        fun onQuestCreated(id: Int)
    }

    @Inject
    lateinit var questRepository: QuestRepository

    @Inject
    lateinit var feedbackHandler: FeedbackHandler

    @Inject
    lateinit var saveUtil: SaveUtil

    @Inject
    lateinit var locationUtil: LocationUtil

    @Inject
    lateinit var resourceUtil: ResourceUtil

    private val currentActivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(KEY_EVENT_BROADCAST_ID)) {
                activityType = intent.getIntExtra(ActivityBroadcastReceiver.KEY_ACTIVITY_TYPE, 0)
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        (applicationContext as MainApplication).appComponent.inject(this)
        isMetric = saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
        serviceIsRunning = true
        startCountDown()

        startLocationUpdates()
        eventHandler =
            ActivityActivator(this)
        eventHandler.startActivityTracking()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            currentActivityReceiver, IntentFilter(KEY_EVENT_BROADCAST_ID)
        )
    }

    private fun startCountDown() {
        var count = 10
        val disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .take(12)
            .map { count - it }
            .doFinally {
                countdownFinished = true
            }
            .subscribe({
                if (it < 0L) {
                    feedbackHandler.speak(resourceUtil.getString(R.string.runage_go_go_go))
                    startNewQuest()
                } else {
                    feedbackHandler.speak(it.toString())
                }
            }, {
                Timber.e(it)
            })
        compositeDisposable.add(disposable)
    }

    private fun startNewQuest(){
        val disposable = questRepository.startNewQuest()
            .subscribe({
                callback?.onQuestCreated(it.id)
                currentQuest = it
            }, {
                Timber.e(it)
            })
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("Service destroyed")
        serviceIsRunning = false
        textToSpeech?.shutdown()
        eventHandler.endActivityTracking()
        locationUtil.removeLocationUpdates(locationCallback)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(currentActivityReceiver)
        compositeDisposable.dispose()
    }

    @SuppressLint("CheckResult")
    private fun handleLocation(location: Location) {
        Observable.just(location)
            .subscribeOn(Schedulers.computation())
            .filter {
                //Filter away crazy values
                Timber.d("Current accuracy: ${it.accuracy}")
                it.accuracy < 25
            }
            .filter {
                if (previousLocation == null) {
                    true
                } else {
                    newPointIsMinDistanceAway(it, previousLocation!!)
                }
            }
            .filter{
                activityType != DetectedActivity.STILL
            }
            .map {
                Timber.d("${it.latitude} ${it.longitude}")
                val newPoint = PositionPoint(
                    it.latitude,
                    it.longitude,
                    it.speed,
                    it.accuracy,
                    it.altitude,
                    it.bearing,
                    it.elapsedRealtimeNanos,
                    activityType
                )
                updateTotalDistance(newPoint)

                if (shouldReport(currentQuest.totalDistance)) {
                    feedbackHandler.reportCheckpoint(currentQuest, nextDistanceFeedback)
                    nextDistanceFeedback++
                }

                val currentTimeMillis = Instant.now().epochSecond
                val currentDurationInSeconds = currentTimeMillis - currentQuest.startTimeEpochSeconds
                val avgPace = RunningTimer.getCurrentPace(currentDurationInSeconds.toDouble()/60, currentQuest.totalDistance/1000)
                currentQuest.pace = avgPace
                currentQuest.locations.add(newPoint)
                Timber.d("Storing ${currentQuest.locations.size} locations")
                questRepository.dbUpdateQuest(currentQuest)
                previousLocation = it
            }
            .subscribe({
                Timber.d("Quest location updated")
            }, {
                Timber.e(it)
            })
    }

    private fun updateTotalDistance(newPoint: PositionPoint) {
        val lastPoint = currentQuest.locations.lastOrNull()
        lastPoint?.let {
            val resultArray = FloatArray(4)
            Location.distanceBetween(
                lastPoint.latitude,
                lastPoint.longitude,
                newPoint.latitude,
                newPoint.longitude,
                resultArray
            )
            currentQuest.totalDistance += resultArray.first()
            currentQuest.calories = CalorieCalculator.getCaloriesBurned(
                distance = currentQuest.totalDistance,
                weight = saveUtil.getFloat(SaveUtil.KEY_WEIGHT)
            )
        }
    }

    private fun shouldReport(totalDistanceInMeters: Double): Boolean {
        return totalDistanceInMeters >= getNextDistanceForReport()
    }

    private fun getNextDistanceForReport(): Double {
        return if (isMetric) {
            nextDistanceFeedback.times(FeedbackHandler.METERS_IN_KILOMETER)
        } else {
            nextDistanceFeedback.times(FeedbackHandler.METERS_IN_MILE)
        }
    }


    private fun newPointIsMinDistanceAway(
        lastLocation: Location,
        previousLocation: Location
    ): Boolean {
        val distance = lastLocation.distanceTo(previousLocation)
        Timber.d("Distance to prev point: $distance")
        return distance >= 1
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result?.lastLocation?.let {
                    Timber.d("LOCATION UPDATE")
                    if (countdownFinished)
                        handleLocation(it)
                }
            }
        }

        locationUtil.requestLocationUpdates(locationRequest, locationCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification: Notification = Notification.Builder(
                    this,
                    CHANNEL_DEFAULT_IMPORTANCE
                )
                .setContentTitle(getText(R.string.notification_title))
                .setChannelId(createNotificationChannel("my_service", "My Background Service"))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_run_blue)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()

            startForeground(NOTIFICATION_ID, notification)
        } else {

            val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getText(R.string.notification_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notification: Notification = builder.build()

            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    fun registerCallback(callback: EventCallback) {
        this.callback = callback
        if (::currentQuest.isInitialized) {
            callback.onQuestCreated(currentQuest.id)
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): EventService = this@EventService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    companion object {
        const val TAG = "Event Service"
        const val NOTIFICATION_ID = 2345235
        const val LOCATION_REQUEST_CODE = 234452
        const val CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE"

        var serviceIsRunning = false
    }
}
