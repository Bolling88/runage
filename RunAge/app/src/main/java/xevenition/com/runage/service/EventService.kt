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
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.ActivityActivator
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_EVENT_BROADCAST_ID
import xevenition.com.runage.MainActivity
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import javax.inject.Inject


class EventService : Service() {

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
            //Here we get the users current activity
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        (applicationContext as MainApplication).appComponent.inject(this)
        serviceIsRunning = true
        //TODO add start delay of 10 seconds
        questRepository.startNewQuest()
            .subscribe({
                callback?.onQuestCreated(it.id)
                currentQuest = it

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                createLocationRequest()
                handleLocationCallbacks(this)
                startLocationUpdates()
                eventHandler = ActivityActivator(this)
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
        serviceIsRunning = false
        eventHandler.endActivityTracking()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(currentActivityReceiver)
        compositeDisposable.dispose()
    }

    private fun handleLocationCallbacks(context: Context) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Timber.d("Got location update")
                locationResult?.let { locationResult ->
                    Observable.just(locationResult)
                        .subscribeOn(Schedulers.io())
                        .filter { it.lastLocation != null }
                        .map {
                            it.lastLocation
                        }
                        .filter {
                            if (previousLocation == null) {
                                previousLocation = it
                                true
                            } else {
                                newPointIsMinDistanceAway(it, previousLocation)
                            }
                        }
                        .map {
                            Timber.d("${it.latitude} ${it.longitude}")
                            currentQuest.locations.add(
                                PositionPoint(
                                    it.latitude,
                                    it.longitude,
                                    it.speed,
                                    it.accuracy,
                                    it.altitude,
                                    it.bearing,
                                    it.elapsedRealtimeNanos
                                )
                            )
                            questRepository.dbUpdateQuest(currentQuest)
                        }
                        .subscribe({
                            Timber.d("Quest location updated")
                        }, {
                            Timber.e(it)
                        })

                }
            }
        }
    }

    private fun newPointIsMinDistanceAway(
        lastLocation: Location,
        previousLocation: Location?
    ): Boolean {
        if (previousLocation == null)
            return true
        val distance = lastLocation.distanceTo(previousLocation)
        return distance >= 20
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
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
        const val NOTIFICATION_ID = 2345235
        const val CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE"

        var serviceIsRunning = false
    }

}
