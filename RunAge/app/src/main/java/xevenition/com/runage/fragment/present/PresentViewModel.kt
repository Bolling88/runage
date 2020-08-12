package xevenition.com.runage.fragment.present

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.SingleLiveEvent

class PresentViewModel(
    gameServicesUtil: GameServicesUtil,
    resourceUtil: ResourceUtil
) : BaseViewModel() {


    private val _liveButtonVisibility = MutableLiveData<Int>()
    val liveButtonVisibility : LiveData<Int> = _liveButtonVisibility

    val observableShowAdd = SingleLiveEvent<Unit>()

    init {

    }

    fun onViewAdClicked(){
        observableShowAdd.call()
    }

    fun onNoClicked(){
        observableBackNavigation.call()
    }

    fun onRewardedAdLoaded() {
        _liveButtonVisibility.postValue(View.VISIBLE)
    }

    fun onAdLoading() {
        _liveButtonVisibility.postValue(View.GONE)
    }
}
