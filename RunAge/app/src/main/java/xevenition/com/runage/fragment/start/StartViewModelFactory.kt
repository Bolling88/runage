package xevenition.com.runage.fragment.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class StartViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory(app) {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var accountUtil: AccountUtil

    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Inject
    lateinit var feedbackHandler: FeedbackHandler

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StartViewModel(accountUtil, resourceUtil, feedbackHandler) as T
    }
}