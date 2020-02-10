package xevenition.com.runage.fragment.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import javax.inject.Inject

class MainViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory(app) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel() as T
    }
}