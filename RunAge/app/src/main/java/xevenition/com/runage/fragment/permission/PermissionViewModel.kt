package xevenition.com.runage.fragment.permission

import android.widget.CompoundButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class PermissionViewModel @Inject constructor(
    private val accountUtil: AccountUtil,
    private val saveUtil: SaveUtil
) : BaseViewModel() {

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
        if (!accountUtil.isAccountActive()) {
            observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToLoginFragment())
        } else if (!saveUtil.getBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED)) {
            observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToSettingsFragment())
        } else {
            observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToMainFragment())
        }
    }

    fun onActivityCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        activityOn = isChecked
        if (isChecked) {
            observableCheckPermissionActivity.call()
        }

        _liveButtonEnabled.postValue(locationOn && activityOn)
    }

    fun onLocationCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        locationOn = isChecked
        if (isChecked) {
            observableCheckPermissionLocation.call()
        }

        _liveButtonEnabled.postValue(locationOn && activityOn)
    }
}
