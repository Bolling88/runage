package xevenition.com.runage.fragment.settings

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.SaveUtil

class SettingsViewModel(
    private val saveUtil: SaveUtil
) : BaseViewModel() {


    private val _liveButtonEnabled = MutableLiveData<Boolean>()
    val liveButtonEnabled: LiveData<Boolean> = _liveButtonEnabled

    fun onMetricClicked() {
        saveUtil.saveBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    }

    fun onImperialClicked() {
        saveUtil.saveBoolean(SaveUtil.KEY_IS_USING_METRIC, false)
    }

    fun onContinueClicked() {
        saveUtil.saveBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED, true)
        observableNavigateTo.postValue(SettingsFragmentDirections.actionSettingsFragmentToMainFragment())
    }

    @SuppressLint("CheckResult")
    @Suppress("UNUSED_PARAMETER")
    fun onWeightTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _liveButtonEnabled.postValue(s.isNotEmpty())
        if (s.isNotEmpty() && s.toString().toFloatOrNull() != null)
            saveUtil.saveFloat(SaveUtil.KEY_WEIGHT, s.toString().toFloat())
    }

}
