package xevenition.com.runage.fragment.main

import android.annotation.SuppressLint
import androidx.core.os.bundleOf
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.settings.SettingsFragmentDirections
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.ResourceUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel(private  val questRepository: QuestRepository) : BaseViewModel() {

    @SuppressLint("CheckResult")
    fun onQuestFinished(questId: Int) {
        questRepository.getSingleQuest(questId)
            .subscribe({
                //Quest exists, show summary
                observableNavigateTo.postValue(MainFragmentDirections.actionMainFragmentToSummaryFragment(questId))
            },{
                //Quest didn't even start, do nothing
            })
    }

    fun onHistoryClicked() {
        observableNavigateTo.postValue(MainFragmentDirections.actionMainFragmentToHistoryFragment())
    }
}
