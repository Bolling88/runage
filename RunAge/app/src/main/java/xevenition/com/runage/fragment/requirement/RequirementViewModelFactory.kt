package xevenition.com.runage.fragment.requirement

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.util.*
import javax.inject.Inject

class RequirementViewModelFactory @Inject constructor(app: MainApplication, private val challenge: Challenge) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var accountUtil: AccountUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var runningUtil: RunningUtil
    @Inject
    lateinit var feedbackHandler: FeedbackHandler
    @Inject
    lateinit var fireStoreHandler: FireStoreHandler
    @Inject
    lateinit var gameServicesUtil: GameServicesUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RequirementViewModel(gameServicesUtil, fireStoreHandler, accountUtil, runningUtil, resourceUtil, feedbackHandler, challenge) as T
    }
}