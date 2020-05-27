package xevenition.com.runage.fragment.history

import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil

class HistoryViewModel(
    resourceUtil: ResourceUtil,
    firestoreHandler: FireStoreHandler
) : BaseViewModel(){


    init {
        firestoreHandler.getAllQuests()
    }
}