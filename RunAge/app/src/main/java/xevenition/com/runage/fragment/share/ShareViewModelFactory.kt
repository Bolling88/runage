package xevenition.com.runage.fragment.share

import xevenition.com.runage.fragment.rule.RuleViewModel

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class ShareViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var gameServicesUtil: GameServicesUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShareViewModel(gameServicesUtil, resourceUtil) as T
    }
}