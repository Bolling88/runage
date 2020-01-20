package xevenition.com.runage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.ActivityTransitionResult
import timber.log.Timber

class ActivityBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Timber.d("Got activity event")
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult.extractResult(intent)?.let {
                Timber.d("Broadcasting activity event")
                val event = it.transitionEvents.last()
                val intent = Intent(KEY_EVENT_BROADCAST_ID)
                intent.putExtra(KEY_ELAPSED_TIME, event.elapsedRealTimeNanos)
                intent.putExtra(KEY_ACTIVITY_TYPE, event.activityType)
                intent.putExtra(KEY_TRANSTITION_TYPE, event.transitionType)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }

    companion object {
        const val KEY_ACTIVITY_TYPE = "KEY_ACTIVITY_TYPE"
        const val KEY_TRANSTITION_TYPE = "KEY_TRANSTITION_TYPE"
        const val KEY_ELAPSED_TIME = "KEY_ELAPSED_TIME"
        const val KEY_EVENT_BROADCAST_ID = "KEY_EVENT_BROADCAST_ID"
    }
}