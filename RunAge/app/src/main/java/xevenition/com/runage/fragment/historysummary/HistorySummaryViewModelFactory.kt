package xevenition.com.runage.fragment.historysummary

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.util.FireStoreHandler
import javax.inject.Inject

class HistorySummaryViewModelFactory @Inject constructor(
    app: MainApplication,
    private val args: HistorySummaryFragmentArgs
) :
    BaseViewModelFactory() {

    init {
        app.appComponent.inject(this)
    }

    @Inject
    lateinit var fireStoreHandler: FireStoreHandler

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistorySummaryViewModel(args) as T
    }
}