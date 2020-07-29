package xevenition.com.runage.fragment.login

import xevenition.com.runage.util.SingleLiveEvent
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.SaveUtil

class LoginViewModel(
    private val saveUtil: SaveUtil,
    private val fireStoreHandler: FireStoreHandler
) : BaseViewModel() {

    private var permissionsGranted = false

    val observableLoginClicked = SingleLiveEvent<Unit>()

    fun onLoginSuccess() {
        fireStoreHandler.storeUserIfNotExists()
        if (!saveUtil.getBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED)) {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToSettingsFragment())
        } else if (!permissionsGranted) {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToPermissionFragment())
        } else {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToMainFragment())
        }
    }

    fun onLoginClicked() {
        observableLoginClicked.call()
    }

    fun permissionsGranted(granted: Boolean) {
        permissionsGranted = granted
    }
}
