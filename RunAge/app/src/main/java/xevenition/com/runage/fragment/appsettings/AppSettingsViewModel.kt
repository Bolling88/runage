package xevenition.com.runage.fragment.appsettings

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.settings.SettingsFragmentDirections
import xevenition.com.runage.util.SaveUtil

class AppSettingsViewModel(
    private val saveUtil: SaveUtil
) : BaseViewModel() {


    private val _liveButtonEnabled = MutableLiveData<Boolean>()
    val liveButtonEnabled: LiveData<Boolean> = _liveButtonEnabled

    @SuppressLint("CheckResult")
    @Suppress("UNUSED_PARAMETER")
    fun onWeightTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _liveButtonEnabled.postValue(s.isNotEmpty())
        if (s.isNotEmpty() && s.toString().toFloatOrNull() != null)
            saveUtil.saveFloat(SaveUtil.KEY_WEIGHT, s.toString().toFloat())
    }

}
