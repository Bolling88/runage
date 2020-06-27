package xevenition.com.runage.fragment.path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.fragment.summary.SummaryFragmentArgs
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.LocationUtil
import javax.inject.Inject

class PathViewModelFactory @Inject constructor(
    app: MainApplication,
    private val arguments: PathFragmentArgs
) : BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var questRepository: QuestRepository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PathViewModel(questRepository, arguments) as T
    }
}