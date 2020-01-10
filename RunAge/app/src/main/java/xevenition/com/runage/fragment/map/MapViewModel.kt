package xevenition.com.runage.fragment.map

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_ACTIVITY_TYPE
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_ELAPSED_TIME
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_TRANSTITION_TYPE
import xevenition.com.runage.R
import xevenition.com.runage.ResourceUtil
import xevenition.com.runage.architecture.BaseViewModel


class MapViewModel(private val resourceUtil: ResourceUtil) : BaseViewModel() {

    private var userMarker: Marker? = null
    val observableAnimateMapPosition = MutableLiveData<CameraUpdate>()
    val observableUserMarkerPosition = MutableLiveData<LatLng>()

    val liveTextActivityType = MutableLiveData<String>()

    fun onUserEventChanged(intent: Intent) {
        val transtitionType = intent.getIntExtra(KEY_TRANSTITION_TYPE, 0)
        val activityType = intent.getIntExtra(KEY_ACTIVITY_TYPE, 0)
        val elapsedTime = intent.getLongExtra(KEY_ELAPSED_TIME, 0)
        displayActivityType(activityType)
    }

    private fun displayActivityType(activityType: Int) {
        when (activityType) {
            DetectedActivity.WALKING -> {
                liveTextActivityType.postValue(resourceUtil.getString(R.string.walking))
                Log.d(TAG, "walking")
            }
            DetectedActivity.RUNNING -> {
                liveTextActivityType.postValue(resourceUtil.getString(R.string.running))
                Log.d(TAG, "running")
            }
            DetectedActivity.IN_VEHICLE -> {
                liveTextActivityType.postValue(resourceUtil.getString(R.string.driving))
                Log.d(TAG, "driving")
            }
            DetectedActivity.ON_BICYCLE -> {
                liveTextActivityType.postValue(resourceUtil.getString(R.string.on_bicycle))
                Log.d(TAG, "cycle")
            }
            DetectedActivity.STILL -> {
                liveTextActivityType.postValue(resourceUtil.getString(R.string.still))
                Log.d(TAG, "still")
            }
        }
    }

    fun passLocationUpdatesObservable(observable: BehaviorSubject<LocationResult>) {
        val disposable = observable.subscribeOn(Schedulers.io())
            .subscribe({
                val lastLocation = it?.lastLocation

                lastLocation?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    val userLocation = CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        19f
                    )
                    observableAnimateMapPosition.postValue(userLocation)
                    observableUserMarkerPosition.postValue(latLng)
                }
            },{
                Log.e(TAG, it.message)
            })
        addDisposable(disposable)
    }

    companion object {
        val TAG = MapViewModel::class.java.name
    }
}
