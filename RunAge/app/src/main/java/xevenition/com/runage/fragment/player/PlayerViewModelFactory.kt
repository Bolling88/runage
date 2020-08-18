package xevenition.com.runage.fragment.player

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject

class PlayerViewModelFactory @Inject constructor(
    app: MainApplication,
    private val playerFragmentArgs: PlayerFragmentArgs
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var runningUtil: RunningUtil
    @Inject
    lateinit var userRepository: UserRepository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(resourceUtil, runningUtil, userRepository, playerFragmentArgs) as T
    }
}