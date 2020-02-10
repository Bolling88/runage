package xevenition.com.runage.fragment.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import javax.inject.Inject

class SettingsViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory(app) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel() as T
    }
}