package xevenition.com.runage.fragment.gifted

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class GiftedViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var saveUtil: SaveUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GiftedViewModel(saveUtil, userRepository) as T
    }
}