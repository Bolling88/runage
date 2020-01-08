package xevenition.com.runage.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.reactivex.disposables.CompositeDisposable
import xevenition.com.runage.ActivityActivator
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_EVENT_BROADCAST_ID
import xevenition.com.runage.MainActivity
import xevenition.com.runage.R
import xevenition.com.runage.fragment.main.MainFragment


class EventService : Service() {

    private lateinit var eventHandler: ActivityActivator
    private val compositeDisposable = CompositeDisposable()

    private val currentActivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Here we get the users current activity
        }
    }

    override fun onCreate() {
        super.onCreate()
        eventHandler = ActivityActivator(this)
        eventHandler.startActivityTracking()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            currentActivityReceiver, IntentFilter(KEY_EVENT_BROADCAST_ID)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification: Notification = Notification.Builder(
                this,
                MainFragment.CHANNEL_DEFAULT_IMPORTANCE
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
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        eventHandler.endActivityTracking()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(currentActivityReceiver)
        compositeDisposable.dispose()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    companion object {
        const val NOTIFICATION_ID = 2345235

        val TAG = EventService::class.java.name
    }

}
