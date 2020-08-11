package xevenition.com.runage.fragment.start

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.service.GameServicesService
import xevenition.com.runage.util.*
import javax.inject.Inject

class StartViewModelFactory @Inject constructor(app: MainApplication) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var gameServicesService: GameServicesService
    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var feedbackHandler: FeedbackHandler
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var gameServicesUtil: GameServicesUtil
    @Inject
    lateinit var saveUtil: SaveUtil

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StartViewModel(gameServicesUtil, gameServicesService, resourceUtil, feedbackHandler, saveUtil, userRepository) as T
    }
}