package xevenition.com.runage.fragment.summary

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.summary.SummaryFragment.Companion.KEY_QUEST_ID
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.RunningTimer
import java.time.Instant

class SummaryViewModel(private val questRepository: QuestRepository, arguments: SummaryFragmentArgs) : BaseViewModel() {
    private val questId = arguments.keyQuestId

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    init {
        //TODO check if imperial or metric
        _liveTextTimer.postValue("00:00:00")
        _liveTotalDistance.postValue("0 m")
        _liveCalories.postValue("0")
        _livePace.postValue("0 min/km")
        getQuest()
    }

    @SuppressLint("CheckResult")
    private fun getQuest(){
        questRepository.getSingleQuest(questId)
            .subscribe({
                setUpQuestInfo(it)
            },{
                Timber.e(it)
            })
    }

    private fun setUpQuestInfo(quest: Quest){
        val lastTimeStamp = quest.locations.lastOrNull()?.timeStampEpochSeconds ?: Instant.now().epochSecond
        val duration = lastTimeStamp - quest.startTimeEpochSeconds
        _liveTextTimer.postValue(RunningTimer.convertTimeToDurationString(duration))
        _liveTotalDistance.postValue("${quest.totalDistance.toInt()} m")
        _liveCalories.postValue("${quest.calories.toInt()}")
        _livePace.postValue("${quest.pace.toInt()} min/km")
    }

    fun onSaveProgressClicked(){

    }
}
