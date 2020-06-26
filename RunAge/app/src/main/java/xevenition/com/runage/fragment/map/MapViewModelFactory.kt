package xevenition.com.runage.fragment.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.LocationUtil
import javax.inject.Inject

class MapViewModelFactory @Inject constructor(app: MainApplication) : BaseViewModelFactory(app) {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var questRepository: QuestRepository
    @Inject
    lateinit var locationUtil: LocationUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(resourceUtil, questRepository, locationUtil) as T
    }
}