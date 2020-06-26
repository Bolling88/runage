package xevenition.com.runage.architecture

import android.widget.BaseAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.fragment.settings.SettingsViewModel
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

open class BaseViewModelFactory(application: MainApplication)  : ViewModelProvider.NewInstanceFactory()