package xevenition.com.runage.fragment.appsettings

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.service.GameServicesService
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class AppSettingsViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var saveUtil: SaveUtil
    @Inject
    lateinit var gameServicesUtil: GameServicesUtil
    @Inject
    lateinit var gameServicesService: GameServicesService

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppSettingsViewModel(saveUtil, gameServicesUtil, gameServicesService) as T
    }
}