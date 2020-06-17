package xevenition.com.runage.fragment.main

import android.annotation.SuppressLint
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.repository.QuestRepository

class MainViewModel(private  val questRepository: QuestRepository) : BaseViewModel() {

    @SuppressLint("CheckResult")
    fun onQuestFinished(questId: Int) {
        questRepository.getSingleQuest(questId)
            .subscribe({
                //Quest exists, show summary
                observableNavigateTo.postValue(MainFragmentDirections.actionMainFragmentToSummaryFragment(questId))
            },{
                //Quest didn't even start, do nothing
            })
    }
}
