package xevenition.com.runage.fragment.map

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.GoogleMap
import xevenition.com.runage.R
import xevenition.com.runage.ResourceUtil
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.service.EventService

class MapViewModel(private val resourceUtil: ResourceUtil) : BaseViewModel() {

    val liveTextActivityType = MutableLiveData<String>()

    fun onMapReady(map: GoogleMap) {

    }

    fun onUserEventChanged(intent: Intent) {
        val transtitionType = intent.getIntExtra(EventService.KEY_TRANSTITION_TYPE, 0)
        val activityType = intent.getIntExtra(EventService.KEY_ACTIVITY_TYPE, 0)
        val elapsedTime = intent.getLongExtra(EventService.KEY_ELAPSED_TIME, 0)
        displayActivityType(activityType)
    }

    private fun displayActivityType(activityType: Int){
        when(activityType){
            DetectedActivity.WALKING ->{
                liveTextActivityType.postValue(resourceUtil.getString(R.string.walking))
                Log.d(TAG, "walking")
            }
            DetectedActivity.RUNNING ->{
                liveTextActivityType.postValue(resourceUtil.getString(R.string.running))
                Log.d(TAG, "running")
            }
            DetectedActivity.IN_VEHICLE ->{
                liveTextActivityType.postValue(resourceUtil.getString(R.string.driving))
                Log.d(TAG, "driving")
            }
            DetectedActivity.ON_BICYCLE ->{
                liveTextActivityType.postValue(resourceUtil.getString(R.string.on_bicycle))
                Log.d(TAG, "cycle")
            }
            DetectedActivity.STILL ->{
                liveTextActivityType.postValue(resourceUtil.getString(R.string.still))
                Log.d(TAG, "still")
            }
        }
    }

    companion object{
        val TAG = MapViewModel::class.java.name
    }
}
