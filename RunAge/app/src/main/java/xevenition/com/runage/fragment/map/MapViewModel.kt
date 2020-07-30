package xevenition.com.runage.fragment.map

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.util.SingleLiveEvent
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
import xevenition.com.runage.util.RunningUtil
import xevenition.com.runage.util.SaveUtil
import java.time.Instant


class MapViewModel(
    private val questRepository: QuestRepository,
    private val locationUtil: LocationUtil,
    private val saveUtil: SaveUtil,
    private val runningUtil: RunningUtil,
    private val resourceUtil: ResourceUtil,
    private val args: MapFragmentArgs?
) : BaseViewModel() {

    private var questId: Int = -1
    private var currentPath: MutableList<LatLng> = mutableListOf()
    private var runningTimerDisposable: Disposable? = null
    private var questDisposable: Disposable? = null
    private var isLocked = true

    private val _observableAnimateMapPosition = MutableLiveData<CameraUpdate>()
    val observableAnimateMapPosition: LiveData<CameraUpdate> = _observableAnimateMapPosition

    private val _observableUserMarkerPosition = MutableLiveData<LatLng>()
    val observableUserMarkerPosition: LiveData<LatLng> = _observableUserMarkerPosition

    private val _observableStartMarkerPosition = MutableLiveData<LatLng>()
    val observableStartMarkerPosition: LiveData<LatLng> = _observableStartMarkerPosition

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _liveButtonClickable = MutableLiveData<Boolean>()
    val liveButtonClickable: LiveData<Boolean> = _liveButtonClickable

    private val _liveLockButtonIconTint = MutableLiveData<Int>()
    val liveLockButtonIconTint: LiveData<Int> = _liveLockButtonIconTint

    private val _liveLockButtonBackgroundTint = MutableLiveData<Int>()
    val liveLockButtonBackgroundTint: LiveData<Int> = _liveLockButtonBackgroundTint

    private val _liveFabVisibility = MutableLiveData<Int>()
    val liveFabVisibility: LiveData<Int> = _liveFabVisibility

    private val _liveImageLock = MutableLiveData<Drawable>()
    val liveImageLock: LiveData<Drawable> = _liveImageLock

    private val _liveTextDistance = MutableLiveData<String>()
    val liveTextDistance: LiveData<String> = _liveTextDistance

    private val _liveTextTime1 = MutableLiveData<String>()
    val liveTextTime1: LiveData<String> = _liveTextTime1

    private val _liveTextTime2 = MutableLiveData<String>()
    val liveTextTime2: LiveData<String> = _liveTextTime2

    private val _liveTextTime3 = MutableLiveData<String>()
    val liveTextTime3: LiveData<String> = _liveTextTime3

    private val _liveCountDownVisibility = MutableLiveData<Int>()
    val liveCountDownVisibility: LiveData<Int> = _liveCountDownVisibility

    private val _liveChallengeInfoVisibility = MutableLiveData<Int>()
    val liveChallengeInfoVisibility: LiveData<Int> = _liveChallengeInfoVisibility

    val observableClearMap = SingleLiveEvent<Unit>()
    val observableStopRun = SingleLiveEvent<Unit>()

    init {
        _liveButtonClickable.postValue(false)
        _liveFabVisibility.postValue(View.VISIBLE)
        _liveLockButtonBackgroundTint.postValue(resourceUtil.getColor(R.color.red))
        _liveLockButtonIconTint.postValue(resourceUtil.getColor(R.color.white))
        _liveImageLock.postValue(resourceUtil.getDrawable(R.drawable.ic_stop))
        _liveChallengeInfoVisibility.postValue(View.GONE)

        args?.keyChallenge?.let {
            _liveTextTime1.postValue(runningUtil.convertTimeToDurationString(it.time.toLong()))
            _liveTextTime2.postValue(runningUtil.convertTimeToDurationString(it.time.toLong() - 20))
            _liveTextTime3.postValue(runningUtil.convertTimeToDurationString(it.time.toLong() - 40))
            _liveTextDistance.postValue(runningUtil.getDistanceString(it.distance))
            _liveChallengeInfoVisibility.postValue(View.VISIBLE)
        }

        resetTimers()
    }

    private fun resetTimers() {
        _liveTextTimer.postValue("00:00:00")
        _liveTotalDistance.postValue("0 m")
        _liveCalories.postValue("0")
        if (saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)) {
            _livePace.postValue("0 ${resourceUtil.getString(R.string.runage_min_km)}")
        } else {
            _livePace.postValue("0 ${resourceUtil.getString(R.string.runage_min_mi)}")
        }
    }

    fun onNewQuestCreated(id: Int) {
        questId = id
        setUpObservableQuest(id)
        _liveCountDownVisibility.postValue(View.GONE)
    }

    private fun setUpObservableQuest(id: Int) {
        Timber.d("Register quest flowable with id: $id")
        questDisposable?.dispose()
        questDisposable = questRepository.getFlowableQuest(id)
            .subscribe({ quest ->
                Timber.d("Got quest update")
                quest.locations.lastOrNull()?.let {
                    moveToCurrentLocation(it)
                }
                displayRunningRoute(quest.locations)

                if (runningTimerDisposable == null || runningTimerDisposable?.isDisposed == true) {
                    setUpRunningTimer(quest.startTimeEpochSeconds)
                }

                _liveTotalDistance.postValue(runningUtil.getDistanceString(quest.totalDistance.toInt()))
                _liveCalories.postValue("${quest.calories}")

                val lastTimeStamp =
                    quest.locations.lastOrNull()?.timeStampEpochSeconds ?: Instant.now().epochSecond
                val duration = lastTimeStamp - quest.startTimeEpochSeconds

                _livePace.postValue(runningUtil.getPaceString(duration, quest.totalDistance, true))
            }, {
                Timber.e(it)
            })
        questDisposable?.let {
            addDisposable(it)
        }
    }

    private fun setUpRunningTimer(startTimeEpochSeconds: Long) {
        runningTimerDisposable = runningUtil.getRunningTimer(startTimeEpochSeconds)
            .subscribe({
                _liveTextTimer.postValue(it)
            }, {
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
            .filter { it.isNotEmpty() }
            .subscribe({
                currentPath = it
                Timber.d("Posting ${it.size} locations")
                _observableStartMarkerPosition.postValue(it.first())
                _observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
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

    fun onStopClicked() {
        observableStopRun.call()
        if (questId == -1) {
            //quest countdown not finished
            observableBackNavigation.call()
        } else {
            onQuestFinished(questId)
        }
    }

    @SuppressLint("CheckResult")
    fun onQuestFinished(questId: Int) {
        questRepository.getSingleQuest(questId)
            .subscribe({
                //Quest exists, show summary
                val challenge = args?.keyChallenge
                if (challenge != null) {
                    observableNavigateTo.postValue(
                        MapFragmentDirections.actionMapFragmentToSummaryFragment(questId)
                    )
                } else {
                    observableNavigateTo.postValue(
                        MapFragmentDirections.actionMapFragmentToSummaryFragment(questId)
                    )
                }
            }, {
                //Quest didn't even start, do nothing
            })
    }

    fun onLockClicked() {
        if (isLocked) {
            isLocked = false
            _liveFabVisibility.postValue(View.GONE)
            _liveButtonClickable.postValue(true)
            _liveLockButtonIconTint.postValue(resourceUtil.getColor(R.color.colorPrimary))
            _liveLockButtonBackgroundTint.postValue(resourceUtil.getColor(R.color.white))
            _liveImageLock.postValue(resourceUtil.getDrawable(R.drawable.ic_lock_open))
        } else {
            isLocked = true
            _liveFabVisibility.postValue(View.VISIBLE)
            _liveButtonClickable.postValue(false)
            _liveLockButtonIconTint.postValue(resourceUtil.getColor(R.color.white))
            _liveLockButtonBackgroundTint.postValue(resourceUtil.getColor(R.color.colorPrimary))
            _liveImageLock.postValue(resourceUtil.getDrawable(R.drawable.ic_lock))
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationUtil.removeLocationUpdates(locationCallback)
    }

    fun onQuestFinished() {
        observableClearMap.call()
        runningTimerDisposable?.dispose()
        resetTimers()
    }
}
