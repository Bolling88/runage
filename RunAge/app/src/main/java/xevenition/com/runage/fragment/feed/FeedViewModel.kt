package xevenition.com.runage.fragment.feed

import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil

class FeedViewModel(
    gameServicesUtil: GameServicesUtil,
    resourceUtil: ResourceUtil
) : BaseViewModel() {
    fun onQuestClicked(quest: SavedQuest) {
        observableNavigateTo.postValue(FeedFragmentDirections.actionFeedFragmentToHistorySummaryFragment(quest))
    }

}
