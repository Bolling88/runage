package xevenition.com.runage.fragment.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.ResourceUtil
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.room.repository.QuestRepository


class MapViewModel(
    private val resourceUtil: ResourceUtil,
    private val questRepository: QuestRepository
) : BaseViewModel() {

    private var currentPath: MutableList<LatLng> = mutableListOf()
    private var questDisposable: Disposable? = null
    val observableAnimateMapPosition = MutableLiveData<CameraUpdate>()
    val observableUserMarkerPosition = MutableLiveData<LatLng>()
    val observableRunningPath = MutableLiveData<List<LatLng>>()

    val observableClearMap = SingleLiveEvent<Unit>()

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
                    displayActivityType(it.activityType)
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
                currentPath = it
                observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
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

    fun onMapCreated() {
        observableRunningPath.postValue(currentPath)
    }

    fun onQuestFinished() {
        observableClearMap.call()
    }

    companion object {
        val TAG = MapViewModel::class.java.name
    }
}
