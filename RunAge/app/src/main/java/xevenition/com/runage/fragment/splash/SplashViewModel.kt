package xevenition.com.runage.fragment.splash

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.service.EventService
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.SaveUtil
import java.util.concurrent.TimeUnit

class SplashViewModel(private val saveUtil: SaveUtil, private val accountUtil: AccountUtil) : BaseViewModel() {

    private var permissionsGranted = false

    init {
        if (EventService.serviceIsRunning) {
            observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToMainFragment())
        } else {
            startSplashTimer()
        }
    }

    private fun startSplashTimer() {
        val disposable = Observable.timer(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .subscribe({
                if (!permissionsGranted) {
                    observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToPermissionFragment())
                } else if (!saveUtil.getBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED)) {
                    observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToSettingsFragment())
                }else if(!accountUtil.isAccountActive()){
                    observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
                } else {
                    observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToMainFragment())
                }
            }, {
                Timber.e(it)
            })
        addDisposable(disposable)
    }

    fun permissionsGranted(granted: Boolean) {
        permissionsGranted = granted
    }
}
