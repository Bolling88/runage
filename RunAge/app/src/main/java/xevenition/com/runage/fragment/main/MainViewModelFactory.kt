package xevenition.com.runage.fragment.main

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.AccountUtil
import javax.inject.Inject

class MainViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory(app) {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var questRepository: QuestRepository
    @Inject
    lateinit var accountUtil: AccountUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(questRepository, accountUtil) as T
    }
}