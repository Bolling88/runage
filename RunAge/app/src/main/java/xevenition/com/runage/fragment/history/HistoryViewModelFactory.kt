package xevenition.com.runage.fragment.history

import android.os.Bundle
import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.room.repository.UserRepository
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class HistoryViewModelFactory @Inject constructor(
    app: MainApplication,
    private val args: Bundle
) :
    BaseViewModelFactory() {

    @Inject
    lateinit var firestoreHandler: FireStoreHandler
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var userRepository: UserRepository

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(resourceUtil, firestoreHandler, userRepository, args) as T
    }
}