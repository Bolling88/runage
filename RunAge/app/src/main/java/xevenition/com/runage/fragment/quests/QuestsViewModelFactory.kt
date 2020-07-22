package xevenition.com.runage.fragment.quests

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class QuestsViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory() {

    @Inject
    lateinit var firestoreHandler: FireStoreHandler
    @Inject
    lateinit var resourceUtil: ResourceUtil

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuestsViewModel(resourceUtil, firestoreHandler) as T
    }
}