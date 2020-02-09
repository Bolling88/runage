package xevenition.com.runage.fragment.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.fragment.settings.SettingsViewModel
import javax.inject.Inject

class PermissionViewModelFactory @Inject constructor() :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel() as T
    }
}