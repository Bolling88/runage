package xevenition.com.runage.fragment.main

import android.annotation.SuppressLint
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.AccountUtil

class MainViewModel(private  val questRepository: QuestRepository,
private val accountUtil: AccountUtil) : BaseViewModel() {

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

    fun onResume() {
       if(!accountUtil.isAccountActive() || accountUtil.getGamesAccount() == null){
           observableBackNavigation.call()
       }
    }
}
