package xevenition.com.runage.fragment.map

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.util.LocationUtil
import xevenition.com.runage.util.RunningUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class MapViewModelFactory @Inject constructor(app: MainApplication, private val args: MapFragmentArgs?) : BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var questRepository: QuestRepository
    @Inject
    lateinit var locationUtil: LocationUtil
    @Inject
    lateinit var saveUtil: SaveUtil
    @Inject
    lateinit var runningUtil: RunningUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(questRepository, locationUtil, saveUtil, runningUtil, resourceUtil, args) as T
    }
}