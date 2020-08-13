package xevenition.com.runage.fragment.present

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import javax.inject.Inject

class PresentViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PresentViewModel() as T
    }
}