package xevenition.com.runage.fragment.summary

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.main.MainFragmentDirections
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.service.FitnessHelper
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import java.time.Instant

class SummaryViewModel(
    private val fitnessHelper: FitnessHelper,
    private val questRepository: QuestRepository,
    private val resourceUtil: ResourceUtil,
    private val fireStoreHandler: FireStoreHandler,
    arguments: SummaryFragmentArgs
) : BaseViewModel() {
    private var mapCreated: Boolean = false
    private var quest: Quest? = null
    private var percentageMap: Map<Int, Double>? = null
    private val questId = arguments.keyQuestId

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _observablePlayAnimation = MutableLiveData<Unit>()
    val observablePlayAnimation: LiveData<Unit> = _observablePlayAnimation

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    private val _liveTimerColor = MutableLiveData<Int>()
    val liveTimerColor: LiveData<Int> = _liveTimerColor

    private val _liveButtonEnabled = MutableLiveData<Boolean>()
    val liveButtonEnabled: LiveData<Boolean> = _liveButtonEnabled

    private val _liveButtonText = MutableLiveData<String>()
    val liveButtonText: LiveData<String> = _liveButtonText

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    private val _liveTextTitle = MutableLiveData<String>()
    val liveTextTitle: LiveData<String> = _liveTextTitle

    private val _observableStartMarker = MutableLiveData<LatLng>()
    val observableStartMarker: LiveData<LatLng> = _observableStartMarker

    private val _observableEndMarker = MutableLiveData<LatLng>()
    val observableEndMarker: LiveData<LatLng> = _observableEndMarker

    init {
        //TODO check if imperial or metric
        _liveButtonEnabled.postValue(false)
        _liveTextTimer.postValue("00:00:00")
        _liveTotalDistance.postValue("0 m")
        _liveCalories.postValue("0")
        _livePace.postValue("0 min/km")
        _liveButtonText.postValue(resourceUtil.getString(R.string.runage_save_progress))
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
        getQuest()
    }

    @SuppressLint("CheckResult")
    private fun getQuest() {
        questRepository.getSingleQuest(questId)
            .subscribe({
                quest = it
                if(mapCreated){
                    displayPath(it)
                }
                setUpQuestInfo(it)
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
        _liveTextTimer.postValue(RunningUtil.convertTimeToDurationString(duration))
        _liveTotalDistance.postValue("${distance.toInt()} m")
        _liveCalories.postValue("${quest.calories}")
        _livePace.postValue(RunningUtil.getPaceString(duration, distance))

        if (quest.locations.size < 2) {
            _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
            _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_quest_failed))
            _liveButtonText.postValue(resourceUtil.getString(R.string.runage_close))
        }else{
            _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_claim_experience))
            _observablePlayAnimation.postValue(Unit)
        }

        RunningUtil.calculateActivityPercentage(quest.locations)
            .doFinally {
                _liveButtonEnabled.postValue(true)
            }
            .subscribe({
                percentageMap = it
                Timber.d("Percentage calculated")
            },{
                Timber.e(it)
            })
    }

    fun onSaveProgressClicked() {
        if (quest?.locations?.size ?: 0 < 2) {
            observableBackNavigation.call()
        } else {
            quest?.let { quest ->
                Timber.d("Start time: ${quest.startTimeEpochSeconds}")
                Timber.d("End time: ${quest.locations.lastOrNull()?.timeStampEpochSeconds
                    ?: quest.startTimeEpochSeconds + 1}")

//                val task = fitnessHelper.storeSession(
//                    quest.id,
//                    "${quest.totalDistance} meters",
//                    quest.startTimeEpochSeconds,
//                    quest.locations.lastOrNull()?.timeStampEpochSeconds
//                        ?: quest.startTimeEpochSeconds + 1
//                )
//                task.addOnCompleteListener {}
                storeQuestInFirestore(quest)
            }
        }
    }

    private fun storeQuestInFirestore(quest: Quest): Disposable {
        return fireStoreHandler.storeQuest(quest)
            .subscribe({
                it.addOnSuccessListener {
                    Timber.d("Quest have been stored")
                    observableBackNavigation.call()
                }
                    .addOnFailureListener { error -> Timber.e("Quest storing failed $error") }
            }, { error ->
                Timber.e("Quest storing failed $error")
            })
    }

    fun onMapCreated() {
        val localQuest = quest
        mapCreated = true
        if (localQuest != null) {
            displayPath(localQuest)
        }
    }

    fun onMapClicked(){
        observableNavigateTo.postValue(SummaryFragmentDirections.actionSummaryFragmentToPathFragment(questId))
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
}
