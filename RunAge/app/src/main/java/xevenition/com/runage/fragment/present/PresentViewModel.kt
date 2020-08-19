package xevenition.com.runage.fragment.present

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.SingleLiveEvent

class PresentViewModel : BaseViewModel() {


    private val _liveButtonVisibility = MutableLiveData<Int>()
    val liveButtonVisibility : LiveData<Int> = _liveButtonVisibility

    val observableShowAdd = SingleLiveEvent<Unit>()

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

    fun onUserEarnedReward() {
      observableNavigateTo.postValue(PresentFragmentDirections.actionPresentFragmentToGiftedFragment())
    }
}
