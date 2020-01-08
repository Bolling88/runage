package xevenition.com.runage

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import javax.inject.Inject

class ActivityActivator @Inject constructor(private val context: Context){

    private var pendingIntent: PendingIntent? = null

    fun startActivityTracking() {
        val transitions = mutableListOf<ActivityTransition>()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_BICYCLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        val request = ActivityTransitionRequest(transitions)
        val intent = Intent(
            context,
            ActivityBroadcastReceiver::class.java
        )
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        val task = ActivityRecognition.getClient(context)
            .requestActivityTransitionUpdates(request, pendingIntent)

        task.addOnSuccessListener {
            // Handle success
            Log.d(TAG, "Registered event receiver")
        }

        task.addOnFailureListener { e: Exception ->
            Log.d(TAG, "Failed registering event receiver")
            Log.d(TAG, e.message)
        }
    }

    fun endActivityTracking() {
        // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
        val task = ActivityRecognition.getClient(context)
            .removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            pendingIntent?.cancel()
        }

        task.addOnFailureListener { e: Exception ->
            Log.e(TAG, e.message)
        }
    }

    companion object{
         val TAG = ActivityActivator::class.java.name
    }
}