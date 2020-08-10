package xevenition.com.runage.fragment.profile

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.GameServicesService
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject

class ProfileViewModelFactory @Inject constructor(
    app: MainApplication,
    private val args: ProfileFragmentArgs
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var runningUtil: RunningUtil
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var gameServicesService: GameServicesService

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(gameServicesService, runningUtil, resourceUtil, userRepository, args) as T
    }
}