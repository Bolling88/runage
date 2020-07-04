package xevenition.com.runage.fragment.permission

import android.widget.BaseAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.fragment.settings.SettingsViewModel
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class PermissionViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PermissionViewModel() as T
    }
}