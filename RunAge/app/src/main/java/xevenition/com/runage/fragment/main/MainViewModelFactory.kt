package xevenition.com.runage.fragment.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.AppModule
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.LocationUtil
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class MainViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory(app) {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var feedbackHandler: FeedbackHandler
    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(feedbackHandler, resourceUtil) as T
    }
}