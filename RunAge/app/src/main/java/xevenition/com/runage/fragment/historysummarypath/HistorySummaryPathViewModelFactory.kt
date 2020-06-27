package xevenition.com.runage.fragment.historysummarypath

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.fragment.path.PathFragmentArgs
import xevenition.com.runage.fragment.path.PathViewModel
import xevenition.com.runage.room.repository.QuestRepository
import javax.inject.Inject

class HistorySummaryPathViewModelFactory @Inject constructor(
    app: MainApplication,
    private val arguments: HistorySummaryPathFragmentArgs
) : BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistorySummaryPathViewModel(arguments) as T
    }
}