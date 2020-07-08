package xevenition.com.runage.fragment.share

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import java.time.Instant


class ShareViewModel(
    private val runningUtil: RunningUtil,
    private val resourceUtil: ResourceUtil,
    private val questRepository: QuestRepository,
    private val arguments: ShareFragmentArgs
) : BaseViewModel() {

    private var quest: Quest? = null
    private var mapIsReady: Boolean = false
    private val questId = arguments.keyQuestId

    private val _observableStartMarker = MutableLiveData<LatLng>()
    val observableStartMarker: LiveData<LatLng> = _observableStartMarker

    private val _observableEndMarker = MutableLiveData<LatLng>()
    val observableEndMarker: LiveData<LatLng> = _observableEndMarker

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    private val _observableMapType = MutableLiveData<Int>()
    val observableMapType: LiveData<Int> = _observableMapType

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _liveTimerColor = MutableLiveData<Int>()
    val liveTimerColor: LiveData<Int> = _liveTimerColor

    val observableShowAd = SingleLiveEvent<Unit>()
    val observableShare = SingleLiveEvent<Unit>()

    init {
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.grey2))
        setUpObservableQuest(questId)
    }

    @SuppressLint("CheckResult")
    private fun setUpObservableQuest(id: Int) {
        Timber.d("Register quest flowable with id: $id")
       questRepository.getSingleQuest(id)
            .subscribe({ quest ->
                this.quest = quest
                if(mapIsReady) {
                    displayPath(quest)
                }
                setUpQuestInfo(quest)
            }, {
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    private fun setUpQuestInfo(quest: Quest) {
        val lastTimeStamp =
            quest.locations.lastOrNull()?.timeStampEpochSeconds ?: Instant.now().epochSecond
        val duration = lastTimeStamp - quest.startTimeEpochSeconds
        val distance = quest.totalDistance
        _liveTextTimer.postValue(runningUtil.convertTimeToDurationString(duration))
        _liveTotalDistance.postValue(runningUtil.getDistanceString(quest.totalDistance.toInt()))
        _liveCalories.postValue("${quest.calories}")
        _livePace.postValue(runningUtil.getPaceString(duration, distance, true))
    }

    @SuppressLint("CheckResult")
    private fun displayPath(quest: Quest) {
        if (quest.locations.isNotEmpty()) {
            val firstLocation = quest.locations.first()
            _observableStartMarker.postValue(
                LatLng(
                    firstLocation.latitude,
                    firstLocation.longitude
                )
            )
        }
        if (quest.locations.size > 1) {
            val lastLocation = quest.locations.last()
            _observableEndMarker.postValue(
                LatLng(
                    lastLocation.latitude,
                    lastLocation.longitude
                )
            )
        }

        Observable.fromIterable(quest.locations)
            .map { LatLng(it.latitude, it.longitude) }
            .toList()
            .subscribe({
                _observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
    }

    fun onCloseClicked() {
        observableBackNavigation.call()
    }

    fun onShareClicked(){
        observableShare.call()
    }

    @SuppressLint("CheckResult")
    private fun deleteLocalQuestAfterSaveCompletion(quest: Quest) {
        Observable.just(quest)
            .subscribeOn(Schedulers.io())
            .subscribe({
                questRepository.dbDeleteQuest(quest)
                Timber.d("All done!")
                observableShowAd.call()
            }, {
                Timber.e(it)
                observableShowAd.call()
            })
    }

    fun onMapCreated() {
        mapIsReady = true
        quest?.let {
            displayPath(it)
        }
    }

    fun onMapNormalClicked(){
        _observableMapType.postValue(GoogleMap.MAP_TYPE_NORMAL)
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.grey2))
    }

    fun onMapSatelliteClicked(){
        _observableMapType.postValue(GoogleMap.MAP_TYPE_SATELLITE)
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.white))
    }

    fun onMapTerrainClicked(){
        _observableMapType.postValue(GoogleMap.MAP_TYPE_TERRAIN)
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.grey2))
    }
}
