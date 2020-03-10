package xevenition.com.runage.fragment.main

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.ResourceUtil
import java.util.concurrent.TimeUnit

class MainViewModel(
    feedbackHandler: FeedbackHandler,
    resourceUtil: ResourceUtil
) : BaseViewModel() {

    init {
        feedbackHandler.speak(resourceUtil.getString(R.string.runage_welcome_back))
    }

}
