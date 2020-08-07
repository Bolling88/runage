package xevenition.com.runage.fragment.share

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject

class ShareViewModelFactory @Inject constructor(
    app: MainApplication,
    private val args: ShareFragmentArgs
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var runningUtil: RunningUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var questRepository: QuestRepository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShareViewModel(runningUtil, resourceUtil, questRepository, args) as T
    }
}