package xevenition.com.runage.fragment.login

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class LoginViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var saveUtil: SaveUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var fireStoreService: FireStoreService

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(saveUtil, resourceUtil, fireStoreService) as T
    }
}