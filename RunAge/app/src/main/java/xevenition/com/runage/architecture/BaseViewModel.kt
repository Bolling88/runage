package xevenition.com.runage.architecture

import androidx.lifecycle.ViewModel
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
}