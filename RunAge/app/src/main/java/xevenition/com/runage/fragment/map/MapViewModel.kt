package xevenition.com.runage.fragment.map

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
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
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.room.repository.QuestRepository


class MapViewModel(
    private val resourceUtil: ResourceUtil,
    private val questRepository: QuestRepository
) : BaseViewModel() {

    private var currentPath: MutableList<LatLng> = mutableListOf()
    private var questDisposable: Disposable? = null

    private val _observableAnimateMapPosition = MutableLiveData<CameraUpdate>()
    val observableAnimateMapPosition: LiveData<CameraUpdate> = _observableAnimateMapPosition
    
    private val _observableUserMarkerPosition = MutableLiveData<LatLng>()
    val observableUserMarkerPosition: LiveData<LatLng> = _observableUserMarkerPosition
    
    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    private val _liveTextActivityType = MutableLiveData<String>()
    val liveTextActivityType: LiveData<String> = _liveTextActivityType

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCurrentAccuracy = MutableLiveData<String>()
    val liveCurrentAccuracy: LiveData<String> = _liveCurrentAccuracy

    val observableClearMap = SingleLiveEvent<Unit>()

    fun onNewQuestCreated(id: Int) {
        setUpObservableQuest(id)
    }

    private fun setUpObservableQuest(id: Int) {
        Timber.d("Register quest flowable with id: $id")
        questDisposable?.dispose()
        questDisposable = questRepository.getFlowableQuest(id)
            .subscribe({ quest ->
                Timber.d("Got quest update")
                quest.locations.lastOrNull()?.let {
                    moveToCurrentLocation(it)
                    displayActivityType(it.activityType)
                }
                displayRunningRoute(quest.locations)
                _liveTotalDistance.postValue("Distance: ${quest.totalDistance} m")
                _liveCurrentAccuracy.postValue("Accuracy: ${quest.locations.lastOrNull()?.accuracy} m")
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
                Timber.d("Posting ${it.size} locations")
                _observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
    }

    private fun displayActivityType(activityType: Int) {
        when (activityType) {
            DetectedActivity.WALKING -> {
                _liveTextActivityType.postValue(resourceUtil.getString(R.string.walking))
                Timber.d("walking")
            }
            DetectedActivity.RUNNING -> {
                _liveTextActivityType.postValue(resourceUtil.getString(R.string.running))
                Timber.d("running")
            }
            DetectedActivity.IN_VEHICLE -> {
                _liveTextActivityType.postValue(resourceUtil.getString(R.string.driving))
                Timber.d("driving")
            }
            DetectedActivity.ON_BICYCLE -> {
                _liveTextActivityType.postValue(resourceUtil.getString(R.string.on_bicycle))
                Timber.d("cycle")
            }
            DetectedActivity.STILL -> {
                _liveTextActivityType.postValue(resourceUtil.getString(R.string.still))
                Timber.d("still")
            }
        }
    }

    private fun moveToCurrentLocation(lastLocation: PositionPoint) {
        val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        val userLocation = CameraUpdateFactory.newLatLngZoom(
            latLng,
            19f
        )
        _observableAnimateMapPosition.postValue(userLocation)
        _observableUserMarkerPosition.postValue(latLng)
    }

    fun onMapCreated() {
        _observableRunningPath.postValue(currentPath)
    }

    fun onQuestFinished() {
        observableClearMap.call()
    }

    companion object {
        val TAG = MapViewModel::class.java.name
    }
}
