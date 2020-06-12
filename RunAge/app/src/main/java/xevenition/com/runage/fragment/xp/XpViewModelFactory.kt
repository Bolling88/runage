package xevenition.com.runage.fragment.xp

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import javax.inject.Inject

class XpViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory(app) {

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return XpViewModel() as T
    }
}