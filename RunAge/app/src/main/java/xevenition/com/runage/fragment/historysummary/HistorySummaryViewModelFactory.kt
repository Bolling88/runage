package xevenition.com.runage.fragment.historysummary

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class HistorySummaryViewModelFactory @Inject constructor(
    app: MainApplication,
    private val args: HistorySummaryFragmentArgs
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var runningUtil: RunningUtil
    @Inject
    lateinit var fireStoreService: FireStoreService
    @Inject
    lateinit var questRepository: QuestRepository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistorySummaryViewModel(args, resourceUtil, runningUtil, fireStoreService, questRepository) as T
    }
}