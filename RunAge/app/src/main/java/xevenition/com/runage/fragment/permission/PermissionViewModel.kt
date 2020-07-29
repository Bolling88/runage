package xevenition.com.runage.fragment.permission

import android.widget.CompoundButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.util.SingleLiveEvent
import xevenition.com.runage.architecture.BaseViewModel
import javax.inject.Inject

class PermissionViewModel @Inject constructor() : BaseViewModel() {

    private var locationOn: Boolean = false
    private var activityOn: Boolean = false

    private val _liveButtonEnabled = MutableLiveData<Boolean>()
    val liveButtonEnabled: LiveData<Boolean> = _liveButtonEnabled

    val observableCheckPermissionActivity = SingleLiveEvent<Unit>()
    val observableCheckPermissionLocation = SingleLiveEvent<Unit>()

    init {
        _liveButtonEnabled.postValue(false)
    }

    fun onContinueClicked() {
        observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToMainFragment())
    }

    fun onActivityCheckChanged(@Suppress("UNUSED_PARAMETER") buttonView: CompoundButton, isChecked: Boolean) {
        if (activityOn != isChecked) {
            activityOn = isChecked
            if (isChecked) {
                observableCheckPermissionActivity.call()
            }

            _liveButtonEnabled.postValue(locationOn && activityOn)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onLocationCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (locationOn != isChecked) {
            locationOn = isChecked
            if (isChecked) {
                observableCheckPermissionLocation.call()
            }

            _liveButtonEnabled.postValue(locationOn && activityOn)
        }
    }
}
