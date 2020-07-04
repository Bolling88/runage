package xevenition.com.runage.fragment.share

import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil


class ShareViewModel(
    private val gameServicesUtil: GameServicesUtil,
    private val resourceUtil: ResourceUtil
) : BaseViewModel() {
    init {
        gameServicesUtil.unlockAchievement(resourceUtil.getString(R.string.achievement_ruler))
    }
}
