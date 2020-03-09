package xevenition.com.runage.fragment.login

import com.bokus.play.util.SingleLiveEvent
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.permission.PermissionFragmentDirections
import xevenition.com.runage.fragment.settings.SettingsFragmentDirections
import xevenition.com.runage.util.SaveUtil

class LoginViewModel(private val saveUtil: SaveUtil) : BaseViewModel() {

    val observableLoginClicked = SingleLiveEvent<Unit>()

    fun onLoginSuccess(){
        if (!saveUtil.getBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED)) {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToSettingsFragment())
        } else {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToMainFragment())
        }
    }

    fun onLoginClicked(){
        observableLoginClicked.call()
    }
}
