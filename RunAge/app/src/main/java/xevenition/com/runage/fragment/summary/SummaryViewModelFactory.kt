package xevenition.com.runage.fragment.summary

import android.os.Bundle
import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.room.repository.QuestRepository
import javax.inject.Inject

class SummaryViewModelFactory @Inject constructor(
    app: MainApplication,
    private val arguments: SummaryFragmentArgs
) :
    BaseViewModelFactory(app) {

    @Inject
    lateinit var questRepository: QuestRepository

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SummaryViewModel(questRepository, arguments) as T
    }
}