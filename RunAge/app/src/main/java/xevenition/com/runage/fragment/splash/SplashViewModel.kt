package xevenition.com.runage.fragment.splash

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.service.EventService
import java.util.concurrent.TimeUnit

class SplashViewModel : BaseViewModel() {

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
                observableNavigateTo.postValue(SplashFragmentDirections.actionSplashFragmentToMainFragment())
            }, {
                Timber.e(it)
            })
        addDisposable(disposable)
    }
}
