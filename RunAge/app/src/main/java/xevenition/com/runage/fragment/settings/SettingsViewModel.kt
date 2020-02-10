package xevenition.com.runage.fragment.settings

import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.architecture.BaseViewModel

class SettingsViewModel : BaseViewModel() {

    val liveButtonEnabled = MutableLiveData<Boolean>()

    init {
        liveButtonEnabled.postValue(false)
    }

    fun onContinueClicked(){
    }

    fun onMetricClicked(){

    }

    fun onImperialClicked(){

    }
}
