package xevenition.com.runage.fragment.summary

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.service.GoogleFitService
import xevenition.com.runage.util.*
import javax.inject.Inject

class SummaryViewModelFactory @Inject constructor(
    app: MainApplication,
    private val arguments: SummaryFragmentArgs
) :
    BaseViewModelFactory() {

    @Inject
    lateinit var questRepository: QuestRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Inject
    lateinit var googleFitService: GoogleFitService

    @Inject
    lateinit var feedbackHandler: FeedbackHandler

    @Inject
    lateinit var locationUtil: LocationUtil

    @Inject
    lateinit var gameServicesUtil: GameServicesUtil

    @Inject
    lateinit var saveUtil: SaveUtil

    @Inject
    lateinit var runningUtil: RunningUtil

    @Inject
    lateinit var gameServicesService: GameServicesService
    @Inject
    lateinit var fireStoreService: FireStoreService

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SummaryViewModel(
            gameServicesUtil,
            gameServicesService,
            googleFitService,
            locationUtil,
            feedbackHandler,
            questRepository,
            resourceUtil,
            saveUtil,
            runningUtil,
            userRepository,
            fireStoreService,
            arguments
        ) as T
    }
}