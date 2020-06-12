package xevenition.com.runage.fragment.xp

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.SaveUtil

class XpViewModel(
) : BaseViewModel() {


    private val _liveButtonEnabled = MutableLiveData<Boolean>()
    val liveButtonEnabled: LiveData<Boolean> = _liveButtonEnabled

}