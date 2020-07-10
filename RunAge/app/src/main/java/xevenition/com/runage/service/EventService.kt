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
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.MainApplication.Companion.serviceIsRunning
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.service.ActivityBroadcastReceiver.Companion.KEY_EVENT_BROADCAST_ID
import xevenition.com.runage.util.*
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.sqrt


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
    private var runningTimerDisposable: Disposable? = null
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

    @Inject
    lateinit var runningUtil: RunningUtil

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
        startInitialCountdown()

        startLocationUpdates()
        eventHandler =
            ActivityActivator(this)
        eventHandler.startActivityTracking()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            currentActivityReceiver, IntentFilter(KEY_EVENT_BROADCAST_ID)
        )
    }

    private fun startInitialCountdown() {
        val count = 10
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

    private fun startNewQuest() {
        val disposable = questRepository.startNewQuest()
            .subscribe({
                callback?.onQuestCreated(it.id)
                currentQuest = it
                setUpRunningTimer(currentQuest.startTimeEpochSeconds)
            }, {
                Timber.e(it)
            })
        compositeDisposable.add(disposable)
    }

    private fun setUpRunningTimer(startTimeEpochSeconds: Long) {
        runningTimerDisposable = runningUtil.getRunningTimer(startTimeEpochSeconds)
            .subscribe({
                Timber.d("New notification message: $it")
                if (this::currentQuest.isInitialized) {
                    val duration = Instant.now().epochSecond - startTimeEpochSeconds

                    val distance =
                        "${resourceUtil.getString(R.string.runage_distance)}: ${runningUtil.getDistanceString(
                            currentQuest.totalDistance.toInt()
                        )}"
                    val calories = "${resourceUtil.getString(R.string.runage_calories)
                        .capitalize()}: ${currentQuest.calories}"
                    val pace =
                        "${resourceUtil.getString(R.string.runage_avg_pace)}: ${runningUtil.getPaceString(
                            duration,
                            currentQuest.totalDistance,
                            true
                        )}"
                    updateNotification(it, "$distance \n$calories \n$pace")
                }
            }, {
                Timber.e(it)
            })
        runningTimerDisposable?.let { compositeDisposable.add(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("Service destroyed")
        serviceIsRunning = false
        textToSpeech?.shutdown()
        runningTimerDisposable?.dispose()
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
                this::currentQuest.isInitialized
            }
            .filter {
                //https://medium.com/@mizutori/make-it-even-better-than-nike-how-to-filter-locations-tracking-highly-accurate-location-in-1c9d53d31d93
                //If location recording is not more than 10 seconds old, we should filter it
                val age = locationUtil.getLocationAge(it)
                (age < 10000)
            }
            .filter {
                //Filter away crazy values
                Timber.d("Current accuracy: ${it.accuracy}")
                it.hasAccuracy() && it.accuracy < MIN_ACCURACY && it.accuracy > 0
            }
            .filter {
                locationUtil.kalmarFilter(location, currentQuest.startTimeEpochSeconds * 1000)
            }
            .map {
                Timber.d("${it.latitude} ${it.longitude}")
                val speed = getSpeed(it)
                val newActivity =
                    //Anti cheat code
                    if ((activityType == DetectedActivity.RUNNING || activityType == DetectedActivity.WALKING) && speed >= 10) {
                        DetectedActivity.IN_VEHICLE
                    } else if (activityType == DetectedActivity.STILL) {
                        if (speed >= 6.5) {
                            DetectedActivity.IN_VEHICLE
                        } else if (speed < 6.5 && speed > 2.0) {
                            DetectedActivity.RUNNING
                        } else if (speed < 2.0 && speed > 0.5) {
                            DetectedActivity.WALKING
                        } else {
                            DetectedActivity.STILL
                        }
                    } else {
                        activityType
                    }

                val newPoint = PositionPoint(
                    it.latitude,
                    it.longitude,
                    it.speed,
                    it.accuracy,
                    it.altitude,
                    it.bearing,
                    Instant.now().epochSecond,
                    newActivity
                )

                updateTotalDistance(newPoint)

                if (shouldReport(currentQuest.totalDistance)) {
                    feedbackHandler.reportCheckpoint(currentQuest, nextDistanceFeedback)
                    nextDistanceFeedback++
                }

                currentQuest.locations.add(newPoint)
                Timber.d("STORING for quest id ${currentQuest.id}:  ${currentQuest.locations.size} locations")
                questRepository.dbUpdateQuest(currentQuest)
                previousLocation = it
            }
            .subscribe({
                Timber.d("Quest location updated")
            }, {
                Timber.e(it)
            })
    }

    private fun getSpeed(location: Location): Float {
       return if (location.hasSpeed() && location.speed > 0) {
            location.speed
        } else {
            previousLocation?.let { lastLocation ->
                // Convert milliseconds to seconds
                val elapsedTimeInSeconds = (location.time - lastLocation.time) / 1000
                val distanceInMeters = lastLocation.distanceTo(location)
                // Speed in m/s
                distanceInMeters / elapsedTimeInSeconds
            } ?: 0f
        }
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

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = UPDATE_INTERVAL
            fastestInterval = UPDATE_INTERVAL
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
        serviceIsRunning = true
        val notification: Notification =
            buildNotification(resourceUtil.getString(R.string.runage_empty_timer), "")

        startForeground(NOTIFICATION_ID, notification)

        if ((runningTimerDisposable == null || runningTimerDisposable?.isDisposed == true) && ::currentQuest.isInitialized) {
            setUpRunningTimer(currentQuest.startTimeEpochSeconds)
        }

        return START_STICKY
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        return NotificationCompat.Builder(
            this,
            CHANNEL_DEFAULT_IMPORTANCE
        )
            .setContentTitle(title)
            .setChannelId(createNotificationChannel("runage_service", "Runage Running Service"))
            .setContentText(content)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setTicker(null)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().setBigContentTitle(title))
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setSmallIcon(R.drawable.ic_run_blue)
            .setColor(resourceUtil.getColor(R.color.colorPrimary))
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = buildNotification(title, content)
        startForeground(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.setSound(null, null)
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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
        const val NOTIFICATION_ID = 2345235
        const val MIN_ACCURACY = 30
        const val UPDATE_INTERVAL = 5000L
        const val CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE"
    }
}
