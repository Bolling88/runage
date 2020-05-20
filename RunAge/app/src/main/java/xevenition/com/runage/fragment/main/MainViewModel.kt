package xevenition.com.runage.fragment.main

import androidx.core.os.bundleOf
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.settings.SettingsFragmentDirections
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.ResourceUtil
import java.util.concurrent.TimeUnit

class MainViewModel : BaseViewModel() {

    fun onQuestFinished(questId: Int) {
        observableNavigateTo.postValue(MainFragmentDirections.actionMainFragmentToSummaryFragment(questId))
    }

}
