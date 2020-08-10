package xevenition.com.runage.fragment.viewpage

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.util.GameServicesService
import javax.inject.Inject

class ViewPageViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var questRepository: QuestRepository
    @Inject
    lateinit var gameServicesService: GameServicesService

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewPageViewModel(questRepository, gameServicesService) as T
    }
}