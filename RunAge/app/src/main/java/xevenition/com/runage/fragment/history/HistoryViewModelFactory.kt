package xevenition.com.runage.fragment.history

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class HistoryViewModelFactory @Inject constructor(
    app: MainApplication) :
    BaseViewModelFactory(app) {

    @Inject
    lateinit var firestoreHandler: FireStoreHandler
    @Inject
    lateinit var resourceUtil: ResourceUtil

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(resourceUtil, firestoreHandler) as T
    }
}