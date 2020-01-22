package xevenition.com.runage.fragment.splash

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.service.EventService
import java.util.concurrent.TimeUnit

class SplashViewModel : BaseViewModel() {

    private var permissionsGranted = false

    init {
        if(EventService.serviceIsRunning){
            observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToMainFragment())
        }else {
            startSplashTimer()
        }
    }

    private fun startSplashTimer() {
        val disposable = Observable.timer(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .subscribe({
                if(permissionsGranted) {
                    observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToMainFragment())
                }else{
                    observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToPermissionFragment())
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
