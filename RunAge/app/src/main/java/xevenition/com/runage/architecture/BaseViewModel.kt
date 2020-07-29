package xevenition.com.runage.architecture

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import xevenition.com.runage.util.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel: ViewModel(){
    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

   protected fun addDisposable(disposable: Disposable){
       compositeDisposable.add(disposable)
   }

    val observableNavigateTo = SingleLiveEvent<NavDirections>()
    val observableBackNavigation = SingleLiveEvent<Unit>()
    val observableToast = SingleLiveEvent<String>()
    val observableInfoDialog = SingleLiveEvent<Triple<String, String, String>>()
}