package xevenition.com.runage.fragment.summary

import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.service.FitnessHelper
import xevenition.com.runage.util.*
import javax.inject.Inject

class SummaryViewModelFactory @Inject constructor(
    app: MainApplication,
    private val arguments: SummaryFragmentArgs
) :
    BaseViewModelFactory(app) {

    @Inject
    lateinit var questRepository: QuestRepository

    @Inject
    lateinit var firestoreHandler: FireStoreHandler

    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Inject
    lateinit var fitnessHelper: FitnessHelper

    @Inject
    lateinit var feedbackHandler: FeedbackHandler

    @Inject
    lateinit var locationUtil: LocationUtil

    @Inject
    lateinit var gameServicesUtil: GameServicesUtil

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SummaryViewModel(
            gameServicesUtil,
            fitnessHelper,
            locationUtil,
            feedbackHandler,
            questRepository,
            resourceUtil,
            firestoreHandler,
            arguments
        ) as T
    }
}