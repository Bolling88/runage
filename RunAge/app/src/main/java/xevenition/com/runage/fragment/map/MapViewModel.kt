package xevenition.com.runage.fragment.map

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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
import xevenition.com.runage.util.LocationUtil
import xevenition.com.runage.util.RunningTimer


class MapViewModel(
    private val resourceUtil: ResourceUtil,
    private val questRepository: QuestRepository,
    private val locationUtil: LocationUtil
) : BaseViewModel() {

    private var currentPath: MutableList<LatLng> = mutableListOf()
    private var runningTimerDisposable: Disposable? = null
    private var questDisposable: Disposable? = null

    private val _observableAnimateMapPosition = MutableLiveData<CameraUpdate>()
    val observableAnimateMapPosition: LiveData<CameraUpdate> = _observableAnimateMapPosition
    
    private val _observableUserMarkerPosition = MutableLiveData<LatLng>()
    val observableUserMarkerPosition: LiveData<LatLng> = _observableUserMarkerPosition
    
    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    private val _liveTextActivityType = MutableLiveData<String>()
    val liveTextActivityType: LiveData<String> = _liveTextActivityType

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCurrentAccuracy = MutableLiveData<String>()
    val liveCurrentAccuracy: LiveData<String> = _liveCurrentAccuracy

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

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

                if(runningTimerDisposable == null){
                    setUpRunningTimer(quest.startTimeMillis)
                }

                //TODO check if imperial or metric
                _liveTotalDistance.postValue("${resourceUtil.getString(R.string.runage_distance)}: ${quest.totalDistance.toInt()} m")
                _liveCalories.postValue("${resourceUtil.getString(R.string.runage_calories)}: ${quest.calories.div(1000).toInt()}")
                _liveCurrentAccuracy.postValue("${resourceUtil.getString(R.string.runage_accuracy)}: ${quest.locations.lastOrNull()?.accuracy?.toInt()} m")
            }, {
                Timber.e(it)
            })
        questDisposable?.let {
            addDisposable(it)
        }
    }

    private fun setUpRunningTimer(startDateMillis: Long){
        runningTimerDisposable = RunningTimer.getRunningTimer(startDateMillis)
            .subscribe({
                _liveTextTimer.postValue(it)
            },{
                Timber.e(it)
            })
        runningTimerDisposable?.let { addDisposable(it) }
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
        updateUserLocationOnMap(lastLocation.latitude, lastLocation.longitude)
    }

    private fun updateUserLocationOnMap(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val userLocation = CameraUpdateFactory.newLatLngZoom(
            latLng,
            19f
        )
        _observableAnimateMapPosition.postValue(userLocation)
        _observableUserMarkerPosition.postValue(latLng)
    }

    fun onMapCreated() {
        _observableRunningPath.postValue(currentPath)
        startLocationUpdates()
    }

    private lateinit var locationCallback: LocationCallback

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result?.let {
                    updateUserLocationOnMap(it.lastLocation.latitude, it.lastLocation.longitude)
                }
            }
        }

        locationUtil.requestLocationUpdates(locationRequest, locationCallback)
    }

    override fun onCleared() {
        super.onCleared()
        locationUtil.removeLocationUpdates(locationCallback)
    }

    fun onQuestFinished() {
        observableClearMap.call()
    }

    companion object {
        val TAG = MapViewModel::class.java.name
    }
}
