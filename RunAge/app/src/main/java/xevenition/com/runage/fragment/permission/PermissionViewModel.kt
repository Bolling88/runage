package xevenition.com.runage.fragment.permission

import android.widget.CompoundButton
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

    val liveButtonEnabled = MutableLiveData<Boolean>()

    val observableCheckPermissionActivity = SingleLiveEvent<Unit>()
    val observableCheckPermissionLocation = SingleLiveEvent<Unit>()

    init {
        liveButtonEnabled.postValue(false)
    }

    fun onContinueClicked() {
        if (!saveUtil.getBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED)) {
            observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToSettingsFragment())
        } else if (!accountUtil.isAccountActive()) {
            observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToLoginFragment())
        } else {
            observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToMainFragment())
        }
    }

    fun onActivityCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        activityOn = isChecked
        if (isChecked) {
            observableCheckPermissionActivity.call()
        }

        liveButtonEnabled.postValue(locationOn && activityOn)
    }

    fun onLocationCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        locationOn = isChecked
        if (isChecked) {
            observableCheckPermissionLocation.call()
        }

        liveButtonEnabled.postValue(locationOn && activityOn)
    }
}
