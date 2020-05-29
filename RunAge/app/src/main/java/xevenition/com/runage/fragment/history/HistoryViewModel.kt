package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
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


    init {
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
            },{
                Timber.e(it)
            })
    }
}