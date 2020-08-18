package xevenition.com.runage.fragment.history

import android.os.Bundle
import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class HistoryViewModelFactory @Inject constructor(
    app: MainApplication,
    private val args: Bundle
) :
    BaseViewModelFactory() {

    @Inject
    lateinit var firestoreService: FireStoreService
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var questRepository: QuestRepository

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(resourceUtil, firestoreService, userRepository, questRepository, args) as T
    }
}