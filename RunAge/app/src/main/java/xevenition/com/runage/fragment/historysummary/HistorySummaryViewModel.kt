package xevenition.com.runage.fragment.historysummary

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.FirestoreLocation
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil
import kotlin.math.roundToInt

class HistorySummaryViewModel(
    args: HistorySummaryFragmentArgs,
    private val resourceUtil: ResourceUtil
) : BaseViewModel() {


    private var mapIsReady: Boolean = false
    private var savedQuest: SavedQuest = args.keySavedQuest

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveRunningProgress = MutableLiveData<Int>()
    val liveRunningProgress: LiveData<Int> = _liveRunningProgress

    private val _liveWalkingProgress = MutableLiveData<Int>()
    val liveWalkingProgress: LiveData<Int> = _liveWalkingProgress

    private val _liveBicyclingProgress = MutableLiveData<Int>()
    val liveBicyclingProgress: LiveData<Int> = _liveBicyclingProgress

    private val _liveStillProgress = MutableLiveData<Int>()
    val liveStillProgress: LiveData<Int> = _liveStillProgress

    private val _liveDrivingProgress = MutableLiveData<Int>()
    val liveDrivingProgress: LiveData<Int> = _liveDrivingProgress

    private val _liveRunningVisibility = MutableLiveData<Int>()
    val liveRunningVisibility: LiveData<Int> = _liveRunningVisibility

    private val _liveWalkingVisibility = MutableLiveData<Int>()
    val liveWalkingVisibility: LiveData<Int> = _liveWalkingVisibility

    private val _liveBicycleVisibility = MutableLiveData<Int>()
    val liveBicycleVisibility: LiveData<Int> = _liveBicycleVisibility

    private val _liveStillVisibility = MutableLiveData<Int>()
    val liveStillVisibility: LiveData<Int> = _liveStillVisibility

    private val _liveDrivingVisibility = MutableLiveData<Int>()
    val liveDrivingVisibility: LiveData<Int> = _liveDrivingVisibility

    private val _liveTextRunningPercentage = MutableLiveData<String>()
    val liveTextRunningPercentage: LiveData<String> = _liveTextRunningPercentage

    private val _liveTextWalkingPercentage = MutableLiveData<String>()
    val liveTextWalkingPercentage: LiveData<String> = _liveTextWalkingPercentage

    private val _liveTextBicyclingPercentage = MutableLiveData<String>()
    val liveTextBicyclingPercentage: LiveData<String> = _liveTextBicyclingPercentage

    private val _liveTextStillPercentage = MutableLiveData<String>()
    val liveTextStillPercentage: LiveData<String> = _liveTextStillPercentage

    private val _liveTextDrivingPercentage = MutableLiveData<String>()
    val liveTextDrivingPercentage: LiveData<String> = _liveTextDrivingPercentage

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    private val _observableStartMarker = MutableLiveData<LatLng>()
    val observableStartMarker: LiveData<LatLng> = _observableStartMarker

    private val _observableEndMarker = MutableLiveData<LatLng>()
    val observableEndMarker: LiveData<LatLng> = _observableEndMarker

    init {
        displayRunningPercentage(savedQuest)
    }

    fun onMapClicked() {
        observableNavigateTo.postValue(
            HistorySummaryFragmentDirections.actionHistorySummaryFragmentToHistorySummaryPathFragment(savedQuest)
        )
    }

    @SuppressLint("CheckResult")
    private fun displayPath(quest: SavedQuest) {
        Observable.just(quest)
            .subscribeOn(Schedulers.computation())
            .map {
                val itemType = object : TypeToken<List<FirestoreLocation>>() {}.type
                Gson().fromJson<List<FirestoreLocation>>(quest.locations, itemType)
            }
            .map {
                if (it.isNotEmpty()) {
                    val firstLocation = it.first()
                    _observableStartMarker.postValue(
                        LatLng(
                            firstLocation.lat,
                            firstLocation.lon
                        )
                    )
                }
                if (it.size > 1) {
                    val lastLocation = it.last()
                    _observableEndMarker.postValue(
                        LatLng(
                            lastLocation.lat,
                            lastLocation.lon
                        )
                    )
                }
                it
            }
            .flatMap { Observable.fromIterable(it) }
            .map { LatLng(it.lat, it.lon) }
            .toList()
            .subscribe({
                _observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
    }

    private fun displayRunningPercentage(quest: SavedQuest){
        if (quest.runningPercentage > 0) {
            val runningPercentage = (quest.runningPercentage*100).roundToInt()
            _liveRunningProgress.postValue(runningPercentage)
            _liveTextRunningPercentage.postValue("${resourceUtil.getString(R.string.runage_running_percentage)} - $runningPercentage")
        } else {
            _liveRunningVisibility.postValue(View.GONE)
        }
        if (quest.walkingPercentage > 0) {
            val walkingPercentage = (quest.walkingPercentage*100).roundToInt()
            _liveWalkingProgress.postValue(walkingPercentage)
            _liveTextWalkingPercentage.postValue("${resourceUtil.getString(R.string.runage_walking_percentage)} - $walkingPercentage")
        } else {
            _liveWalkingVisibility.postValue(View.GONE)
        }
        if (quest.bicyclingPercentage > 0) {
            val bicyclingPercentage = (quest.bicyclingPercentage*100).roundToInt()
            _liveBicyclingProgress.postValue(bicyclingPercentage)
            _liveTextBicyclingPercentage.postValue("${resourceUtil.getString(R.string.runage_bicycling_percentage)} - $bicyclingPercentage")
        } else {
            _liveBicycleVisibility.postValue(View.GONE)
        }
        if (quest.stillPercentage > 0) {
            val stillPercentage = (quest.stillPercentage*100).roundToInt()
            _liveStillProgress.postValue((quest.stillPercentage*100).roundToInt())
            _liveTextStillPercentage.postValue("${resourceUtil.getString(R.string.runage_still_percentage)} - $stillPercentage")
        } else {
            _liveStillVisibility.postValue(View.GONE)
        }
        if (quest.drivingPercentage > 0) {
            val drivingPercentage = (quest.drivingPercentage*100).roundToInt()
            _liveDrivingProgress.postValue(drivingPercentage)
            _liveTextDrivingPercentage.postValue("${resourceUtil.getString(R.string.runage_driving_percentage)} - $drivingPercentage")
        } else {
            _liveDrivingVisibility.postValue(View.GONE)
        }
    }

    fun onCloseClicked() {
        observableBackNavigation.call()
    }

    fun onMapCreated() {
        mapIsReady = true
        displayPath(savedQuest)
    }

}
