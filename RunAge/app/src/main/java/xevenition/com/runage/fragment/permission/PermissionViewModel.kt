package xevenition.com.runage.fragment.permission

import android.os.Build
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.SaveUtil
import javax.inject.Inject

class PermissionViewModel @Inject constructor(
    private val resourceUtil: ResourceUtil,
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
        observableNavigateTo.postValue(PermissionFragmentDirections.actionPermissionFragmentToMainFragment())
    }

    fun onActivityCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (activityOn != isChecked) {
            activityOn = isChecked
            if (isChecked) {
                observableCheckPermissionActivity.call()
            }

            _liveButtonEnabled.postValue(locationOn && activityOn)
        }
    }

    fun onLocationCheckChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (locationOn != isChecked) {
            locationOn = isChecked
            if (isChecked) {
                observableCheckPermissionLocation.call()
            }

            _liveButtonEnabled.postValue(locationOn && activityOn)
        }
    }

    fun onQAlwaysBackgroundDenied() {
        observableInfoDialog.postValue(
            Triple(
                resourceUtil.getString(R.string.runage_allow_always),
                resourceUtil.getString(R.string.runage_location_android_q),
                resourceUtil.getString(R.string.runage_ok)
            )
        )
    }
}
