package xevenition.com.runage.fragment.support

import xevenition.com.runage.fragment.appsettings.AppSettingsViewModel

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class SupportViewModelFactory @Inject constructor(
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
    lateinit var accountUtil: AccountUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SupportViewModel(saveUtil, gameServicesUtil, accountUtil) as T
    }
}