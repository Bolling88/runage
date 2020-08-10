package xevenition.com.runage.fragment.path

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.QuestRepository
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