package xevenition.com.runage.fragment.feed

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class FeedViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var gameServicesUtil: GameServicesUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var userRepository: UserRepository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FeedViewModel(gameServicesUtil, resourceUtil, userRepository) as T
    }
}