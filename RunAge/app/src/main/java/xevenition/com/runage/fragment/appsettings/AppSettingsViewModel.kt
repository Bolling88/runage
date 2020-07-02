package xevenition.com.runage.fragment.appsettings

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.SaveUtil


class AppSettingsViewModel(
    private val saveUtil: SaveUtil,
    private val gameServicesUtil: GameServicesUtil,
    private val accountUtil: AccountUtil
) : BaseViewModel() {


    private val _liveButtonEnabled = MutableLiveData<Boolean>()
    val liveButtonEnabled: LiveData<Boolean> = _liveButtonEnabled

    private val _liveSyncGoogleFit = MutableLiveData<Boolean>()
    val liveSyncGoogleFit: LiveData<Boolean> = _liveSyncGoogleFit

    private val _observableWeight = MutableLiveData<Float>()
    val observableWeight: LiveData<Float> = _observableWeight

    private val _observableUnitType = MutableLiveData<Boolean>()
    val observableUnitType: LiveData<Boolean> = _observableUnitType

    val observableCloseApp = SingleLiveEvent<Unit>()
    val observableShowAchievements = SingleLiveEvent<Intent>()

    init {
        _observableWeight.postValue(saveUtil.getFloat(SaveUtil.KEY_WEIGHT, 0f))
        _liveSyncGoogleFit.postValue(saveUtil.getBoolean(SaveUtil.KEY_SYNC_GOOGLE_FIT, true))
        _observableUnitType.postValue(saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true))
    }

    @SuppressLint("CheckResult")
    @Suppress("UNUSED_PARAMETER")
    fun onWeightTextChanged(weight: String) {
        val newWeight = weight.toFloatOrNull()
        if (newWeight != null && newWeight != _observableWeight.value && newWeight != 0f) {
            saveUtil.saveFloat(SaveUtil.KEY_WEIGHT, weight.toFloat())
        }
    }

    fun onMetricClicked() {
        saveUtil.saveBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    }

    fun onImperialClicked() {
        saveUtil.saveBoolean(SaveUtil.KEY_IS_USING_METRIC, false)
    }

    fun onSignOutClicked(){
        Timber.d("onSignOutClicked")
        gameServicesUtil.signOut().addOnCompleteListener {
            Timber.d("Sign out done")
            observableCloseApp.call()
        }
    }

    fun onProfileClicked(){
        accountUtil.getGamesPlayerInfo()?.addOnSuccessListener { player ->
            accountUtil.getPlayerProfileIntent(player)?.addOnSuccessListener {
                observableShowAchievements.postValue(it)
            }
        }
    }

    fun onFitCheckChanged(checked: Boolean) {
        saveUtil.saveBoolean(SaveUtil.KEY_SYNC_GOOGLE_FIT, checked)
    }

}
