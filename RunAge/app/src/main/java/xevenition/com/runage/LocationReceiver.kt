package xevenition.com.runage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationResult
import timber.log.Timber


class LocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Got location event")
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            val location = locationResult.lastLocation
            if (location != null) {
                Timber.d("Broadcasting location event")
                val intent = Intent(KEY_LOCATION_BROADCAST_ID)
                intent.putExtra(KEY_LOCATION, location)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            } else {
                Timber.d(
                    "*** location object is null ***"
                )
            }
        }
    }

    companion object {
        const val KEY_LOCATION_BROADCAST_ID = "KEY_LOCATION_BROADCAST_ID"
        const val KEY_LOCATION = "KEY_LOCATION"
    }
}
