package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil

class HistoryViewModel(
    resourceUtil: ResourceUtil,
    firestoreHandler: FireStoreHandler
) : BaseViewModel(){

    private val _observableQuests = MutableLiveData<List<SavedQuest>>()
    val observableQuest: LiveData<List<SavedQuest>> = _observableQuests

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)
            firestoreHandler.getAllQuests()
                .addOnSuccessListener { collection ->
                    if (collection != null) {
                        Timber.d("DocumentSnapshot data: ${collection.documents}")
                        processQuests(collection)
                    } else {
                        Timber.d("No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.e("get failed with $exception")
                }
    }

    @SuppressLint("CheckResult")
    private fun processQuests(collection: QuerySnapshot){
        Observable.just(collection)
            .subscribeOn(Schedulers.computation())
            .map {
                collection.toObjects(SavedQuest::class.java)
            }
            .subscribe({
                Timber.d("Quests processed")
                _observableQuests.postValue(it)
                _liveProgressVisibility.postValue(View.GONE)

                if(it.isEmpty()){
                    _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                }
            },{
                Timber.e(it)
            })
    }
}