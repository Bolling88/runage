package xevenition.com.runage.fragment.login

import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.settings.SettingsFragmentDirections
import xevenition.com.runage.util.SaveUtil

class LoginViewModel(private val saveUtil: SaveUtil) : BaseViewModel() {

    fun onMetricClicked(){
        saveUtil.saveBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    }

    fun onImperialClicked(){
        saveUtil.saveBoolean(SaveUtil.KEY_IS_USING_METRIC, false)
    }

    fun onContinueClicked(){
        saveUtil.saveBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED, true)
        observableNavigateTo.postValue(SettingsFragmentDirections.actionSettingsFragmentToMainFragment())
    }
}
