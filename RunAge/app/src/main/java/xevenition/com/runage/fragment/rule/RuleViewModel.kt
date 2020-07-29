package xevenition.com.runage.fragment.rule

import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil


class RuleViewModel(
      gameServicesUtil: GameServicesUtil,
      resourceUtil: ResourceUtil
) : BaseViewModel() {
    init {
        gameServicesUtil.unlockAchievement(resourceUtil.getString(R.string.achievement_ruler))
    }
}
