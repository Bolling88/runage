package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
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
    private val firestoreHandler: FireStoreHandler
) : BaseViewModel() {

    private var lastSnapshot: DocumentSnapshot? = null
    private var allQuests = mutableListOf<SavedQuest>()

    private val _observableQuests = MutableLiveData<List<SavedQuest>>()
    val observableQuest: LiveData<List<SavedQuest>> = _observableQuests

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)
        firestoreHandler.loadQuestsMine()
            .addOnSuccessListener { collection ->
                if (collection != null && !collection.isEmpty) {
                    lastSnapshot = collection.documents.last()
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
    private fun processQuests(collection: QuerySnapshot) {
        Observable.just(collection)
            .subscribeOn(Schedulers.computation())
            .map {
                collection.toObjects(SavedQuest::class.java)
            }
            .subscribe({
                Timber.d("Quests processed")
                val newList = allQuests.toMutableList()
                newList.addAll(it)
                _observableQuests.postValue(newList)
                allQuests = newList
                _liveProgressVisibility.postValue(View.GONE)

                if (allQuests.isEmpty()) {
                    _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                }
            }, {
                Timber.e(it)
            })
    }

    fun onQuestClicked(quest: SavedQuest) {
        observableNavigateTo.postValue(
            HistoryFragmentDirections.actionHistoryFragmentToHistorySummaryFragment(quest)
        )
    }

    fun loadMoreData() {
        Timber.d("Load more data")
        lastSnapshot?.let {
            firestoreHandler.loadQuestsMineMore(it)
                .addOnSuccessListener { collection ->
                    if (collection != null && !collection.isEmpty) {
                        lastSnapshot = collection.documents.last()
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
    }

    fun onReachedItem(position: Int) {
        Timber.d("Reached position $position")
        Timber.d("Items size ${allQuests.size}")
        if(position == allQuests.size-1){
            loadMoreData()
        }
    }
}