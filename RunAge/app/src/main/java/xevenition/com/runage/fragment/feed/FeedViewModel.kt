package xevenition.com.runage.fragment.feed

import android.annotation.SuppressLint
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil

class FeedViewModel(
    gameServicesUtil: GameServicesUtil,
    resourceUtil: ResourceUtil,
    private val userRepository: UserRepository
) : BaseViewModel() {
    @SuppressLint("CheckResult")
    fun onQuestClicked(quest: SavedQuest) {
        userRepository.getSingleUser()
            .subscribe({
                if(quest.userId == it.userId) {
                    observableNavigateTo.postValue(
                        FeedFragmentDirections.actionFeedFragmentToHistorySummaryFragment(
                            quest
                        )
                    )
                }else{
                    observableNavigateTo.postValue(FeedFragmentDirections.actionFeedFragmentToPlayerFragment(quest))
                }
            },{
                Timber.e(it)
            })
    }

}
