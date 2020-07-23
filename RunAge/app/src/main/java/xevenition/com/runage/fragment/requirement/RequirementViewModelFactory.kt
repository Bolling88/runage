package xevenition.com.runage.fragment.requirement

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.*
import javax.inject.Inject

class RequirementViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var accountUtil: AccountUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var feedbackHandler: FeedbackHandler
    @Inject
    lateinit var fireStoreHandler: FireStoreHandler
    @Inject
    lateinit var gameServicesUtil: GameServicesUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RequirementViewModel(gameServicesUtil, fireStoreHandler, accountUtil, resourceUtil, feedbackHandler) as T
    }
}