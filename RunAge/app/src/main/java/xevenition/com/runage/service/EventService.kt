package xevenition.com.runage.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import io.reactivex.disposables.CompositeDisposable
import xevenition.com.runage.ActivityActivator
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_EVENT_BROADCAST_ID
import xevenition.com.runage.MainActivity
import xevenition.com.runage.R


class EventService : Service() {

    private var locationRequest: LocationRequest? = null
    private lateinit var eventHandler: ActivityActivator
    private val compositeDisposable = CompositeDisposable()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val currentActivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Here we get the users current activity
        }
    }

    override fun onCreate() {
        super.onCreate()
        serviceIsRunning = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        handleLocationCallbacks(this)
        startLocationUpdates()
        eventHandler = ActivityActivator(this)
        eventHandler.startActivityTracking()

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
                Log.d(TAG, "Got location update")
                locationResult ?: return
                val intent = Intent(KEY_LOCATION_BROADCAST_ID)
                intent.putExtra(
                    KEY_LOCATION, locationResult
                )
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
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
            interval = 10000
            fastestInterval = 5000
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    companion object {
        const val NOTIFICATION_ID = 2345235
        const val CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE"
        const val KEY_LOCATION = "KEY_LOCATION"
        const val KEY_LOCATION_BROADCAST_ID = "KEY_LOCATION_BROADCAST_ID"

        private val TAG = EventService::class.java.name

        var serviceIsRunning = false
    }

}