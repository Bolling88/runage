package xevenition.com.runage.fragment.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import javax.inject.Inject

class SplashViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory(app) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashViewModel() as T
    }
}