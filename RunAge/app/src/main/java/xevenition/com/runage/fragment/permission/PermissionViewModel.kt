package xevenition.com.runage.fragment.permission

import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import xevenition.com.runage.architecture.BaseViewModel

class PermissionViewModel : BaseViewModel() {

    val liveButtonEnabled = MutableLiveData<Boolean>()

    val observableCheckPermissionActivity = SingleLiveEvent<Unit>()

    init {
        liveButtonEnabled.postValue(false)
    }

    fun onContinueClicked(){

    }

    fun onActivityCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if(isChecked){
            observableCheckPermissionActivity.call()
        }
    }
}
