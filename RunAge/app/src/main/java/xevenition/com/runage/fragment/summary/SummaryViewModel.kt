package xevenition.com.runage.fragment.summary

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningTimer
import xevenition.com.runage.util.SeparatorUtil
import java.time.Instant

class SummaryViewModel(
    private val questRepository: QuestRepository,
    private val resourceUtil: ResourceUtil,
    private val fireStoreHandler: FireStoreHandler,
    arguments: SummaryFragmentArgs
) : BaseViewModel() {
    private var quest: Quest? = null
    private val questId = arguments.keyQuestId

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    private val _liveTimerColor = MutableLiveData<Int>()
    val liveTimerColor: LiveData<Int> = _liveTimerColor

    private val _liveButtonText = MutableLiveData<String>()
    val liveButtonText: LiveData<String> = _liveButtonText

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    init {
        //TODO check if imperial or metric
        _liveTextTimer.postValue("00:00:00")
        _liveTotalDistance.postValue("0 m")
        _liveCalories.postValue("0")
        _livePace.postValue("0 min/km")
        _liveButtonText.postValue(resourceUtil.getString(R.string.runage_save_progress))
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.grey2))
        getQuest()
    }

    @SuppressLint("CheckResult")
    private fun getQuest() {
        questRepository.getSingleQuest(questId)
            .subscribe({
                quest = it
                setUpQuestInfo(it)
            }, {
                Timber.e(it)
            })
    }

    private fun setUpQuestInfo(quest: Quest) {
        val lastTimeStamp =
            quest.locations.lastOrNull()?.timeStampEpochSeconds ?: Instant.now().epochSecond
        val duration = lastTimeStamp - quest.startTimeEpochSeconds
        val distance = quest.totalDistance
        _liveTextTimer.postValue(RunningTimer.convertTimeToDurationString(duration))
        _liveTotalDistance.postValue("$distance m")
        _liveCalories.postValue("${quest.calories}")
        _livePace.postValue(RunningTimer.getPaceString(duration, distance))

        if (quest.locations.size < 2) {
            _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
            _liveButtonText.postValue(resourceUtil.getString(R.string.runage_close))
        }
    }

    fun onSaveProgressClicked() {
        if (quest?.locations?.size ?: 0 < 2) {
            observableBackNavigation.call()
        }else {
            quest?.let { quest ->
                fireStoreHandler.storeQuest(quest)
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
        }
    }

    fun onMapCreated() {
        val localQuest = quest
        if(localQuest != null) {
            displayPath(localQuest)
        }
    }

    @SuppressLint("CheckResult")
    private fun displayPath(localQuest: Quest) {
        Observable.fromIterable(localQuest.locations)
            .map { LatLng(it.latitude, it.longitude) }
            .toList()
            .subscribe({
                _observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
    }
}
