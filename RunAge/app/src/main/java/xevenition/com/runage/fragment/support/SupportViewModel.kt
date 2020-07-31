package xevenition.com.runage.fragment.support


import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.SaveUtil
import xevenition.com.runage.util.SingleLiveEvent

class SupportViewModel(
    private val saveUtil: SaveUtil,
    private val gameServicesUtil: GameServicesUtil,
    private val accountUtil: AccountUtil
) : BaseViewModel(){

    val observableSendEmail = SingleLiveEvent<Unit>()
    val observableOpenBrowser = SingleLiveEvent<Unit>()

    fun onEmailClicked(){
        observableSendEmail.call()
    }

    fun onSupportClicked(){
        observableOpenBrowser.call()
    }
}
