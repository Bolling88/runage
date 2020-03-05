package xevenition.com.runage.fragment.login

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.fragment.settings.SettingsViewModel
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class LoginViewModelFactory @Inject constructor(private val app: MainApplication) :
    BaseViewModelFactory(app) {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var saveUtil: SaveUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(saveUtil) as T
    }
}