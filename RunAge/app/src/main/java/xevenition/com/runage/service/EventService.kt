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
import java.util.*
import javax.inject.Inject


class EventService : Service() {

    private var textToSpeech: TextToSpeech? = null
    // private var wakeLock: PowerManager.WakeLock? = null
    private var activityType: Int = DetectedActivity.WALKING
    private lateinit var currentQuest: Quest
    private var locationRequest: LocationRequest? = null
    private var previousLocation: Location? = null
    private lateinit var eventHandler: ActivityActivator
    private val compositeDisposable = CompositeDisposable()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val binder = LocalBinder()
    private var callback: EventCallback? = null

    interface EventCallback {
        fun onQuestCreated(id: Int)
    }

    @Inject
    lateinit var questRepository: QuestRepository

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

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            textToSpeech?.language = Locale.US
            textToSpeech?.speak("Fuck yeah lets go mother fucker", TextToSpeech.QUEUE_ADD, null,null)
        })

        serviceIsRunning = true
        //TODO add start delay of 10 seconds
        questRepository.startNewQuest()
            .subscribe({
                callback?.onQuestCreated(it.id)
                currentQuest = it

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                createLocationRequest()
                startLocationUpdates()
                eventHandler =
                    ActivityActivator(this)
                eventHandler.startActivityTracking()
            }, {
                Timber.e(it)
            })

        LocalBroadcastManager.getInstance(this).registerReceiver(
            currentActivityReceiver, IntentFilter(KEY_EVENT_BROADCAST_ID)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        //wakeLock?.release()
        Timber.e("Service destroyed")
        serviceIsRunning = false
        textToSpeech?.shutdown()
        eventHandler.endActivityTracking()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(currentActivityReceiver)
        compositeDisposable.dispose()
    }

    @SuppressLint("CheckResult")
    private fun handleLocation(location: Location) {
        Observable.just(location)
            .subscribeOn(Schedulers.computation())
//            .filter {
//                //Filter away crazy values
//                Timber.d("Current accuracy: ${it.accuracy}")
//                it.accuracy < 20
//            }
//            .filter {
//                if (previousLocation == null) {
//                    true
//                } else {
//                    newPointIsMinDistanceAway(it, previousLocation!!)
//                }
//            }
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
//        val intent = Intent(this, LocationReceiver::class.java)
//        val locationIntent = PendingIntent.getService(
//            applicationContext,
//            LOCATION_REQUEST_CODE,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result?.lastLocation?.let {
                    Timber.d("LOCATION UPDATE")
                    handleLocation(it)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, this.mainLooper
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
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
