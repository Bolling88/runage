package xevenition.com.runage

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationResult
import timber.log.Timber


class LocationReceiver : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        Timber.d("Got location event")
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            val location = locationResult.lastLocation
            if (location != null) {
                Timber.d("Broadcasting location event")
                val intent = Intent(KEY_LOCATION_BROADCAST_ID)
                intent.putExtra(KEY_LOCATION, location)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            } else {
                Timber.d(
                    "*** location object is null ***"
                )
            }
        }
    }

    companion object {
        const val TAG = "LocationReceiver"
        const val KEY_LOCATION_BROADCAST_ID = "KEY_LOCATION_BROADCAST_ID"
        const val KEY_LOCATION = "KEY_LOCATION"
    }
}
