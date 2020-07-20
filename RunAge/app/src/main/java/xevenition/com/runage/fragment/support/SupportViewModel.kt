package xevenition.com.runage.fragment.support


import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.SaveUtil

class SupportViewModel(
    private val saveUtil: SaveUtil,
    private val gameServicesUtil: GameServicesUtil,
    private val accountUtil: AccountUtil
) : BaseViewModel() {

}
