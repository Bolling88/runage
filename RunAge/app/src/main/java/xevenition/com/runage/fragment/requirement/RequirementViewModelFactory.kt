package xevenition.com.runage.fragment.requirement

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject

class RequirementViewModelFactory @Inject constructor(app: MainApplication, private val args: RequirementFragmentArgs) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var runningUtil: RunningUtil
    @Inject
    lateinit var feedbackHandler: FeedbackHandler

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RequirementViewModel(runningUtil, resourceUtil, feedbackHandler, args) as T
    }
}