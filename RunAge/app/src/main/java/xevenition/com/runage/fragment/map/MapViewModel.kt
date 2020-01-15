package xevenition.com.runage.fragment.map

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_ACTIVITY_TYPE
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_ELAPSED_TIME
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_TRANSTITION_TYPE
import xevenition.com.runage.R
import xevenition.com.runage.ResourceUtil
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.room.repository.QuestRepository


class MapViewModel(
    private val resourceUtil: ResourceUtil,
    private val questRepository: QuestRepository
) : BaseViewModel() {

    private var questDisposable: Disposable? = null
    val observableAnimateMapPosition = MutableLiveData<CameraUpdate>()
    val observableUserMarkerPosition = MutableLiveData<LatLng>()
    val observableRunningPath = MutableLiveData<List<LatLng>>()

    val liveTextActivityType = MutableLiveData<String>()

    fun onNewQuestCreated(id: Int) {
        setUpObservableQuest(id)
    }

    private fun setUpObservableQuest(id: Int) {
        Timber.d("Register quest flowable with id: $id")
        questDisposable?.dispose()
        questDisposable = questRepository.getFlowableQuest(id)
            .subscribe({ quest ->
                Timber.d("Got quest update")
                for (loc in quest.locations) {
                    Timber.d("${loc.latitude} ${loc.longitude}")
                }
                quest.locations.lastOrNull()?.let {
                    moveToCurrentLocation(it)
                }
                displayRunningRoute(quest.locations)
            }, {
                Timber.e(it)
            })
        questDisposable?.let {
            addDisposable(it)
        }
    }

    @SuppressLint("CheckResult")
    private fun displayRunningRoute(locations: MutableList<PositionPoint>) {
        Observable.fromIterable(locations)
            .subscribeOn(Schedulers.computation())
            .map {
                LatLng(it.latitude, it.longitude)
            }
            .toList()
            .subscribe({
                observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
    }

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

    private fun moveToCurrentLocation(lastLocation: PositionPoint) {
        val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        val userLocation = CameraUpdateFactory.newLatLngZoom(
            latLng,
            19f
        )
        observableAnimateMapPosition.postValue(userLocation)
        observableUserMarkerPosition.postValue(latLng)
    }

    companion object {
        val TAG = MapViewModel::class.java.name
    }
}
