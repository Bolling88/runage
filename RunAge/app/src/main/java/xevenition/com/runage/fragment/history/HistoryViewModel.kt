package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.KEY_PAGE
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_ALL
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_FOLLOWING
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_MINE
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.LevelCalculator
import xevenition.com.runage.util.ResourceUtil

class HistoryViewModel(
    resourceUtil: ResourceUtil,
    private val firestoreHandler: FireStoreHandler,
    args: Bundle
) : BaseViewModel() {

    private var userInfo: UserInfo? = null
    private var lastSnapshot: DocumentSnapshot? = null
    private var allQuests = mutableListOf<SavedQuest>()

    private val _observableQuests = MutableLiveData<List<SavedQuest>>()
    val observableQuest: LiveData<List<SavedQuest>> = _observableQuests

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    private val page = args.getInt(KEY_PAGE)

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)

        //TODO replace with ROOM
        firestoreHandler.getUserInfo()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userInfo = document.toObject(UserInfo::class.java)
                    val userInfo = userInfo
                    if (userInfo != null) {
                        when (page) {
                            PAGE_MINE -> firestoreHandler.loadQuestsMine(userInfo)
                            PAGE_FOLLOWING -> firestoreHandler.loadQuestsFollowing(userInfo)
                            PAGE_ALL -> firestoreHandler.loadQuestsAll()
                            else -> null
                        }?.addOnSuccessListener { collection ->
                            if (collection != null && !collection.isEmpty) {
                                lastSnapshot = collection.documents.last()
                                Timber.d("DocumentSnapshot data: ${collection.documents}")
                                processQuests(collection)
                            } else {
                                Timber.d("No such document")
                            }
                        }
                            ?.addOnFailureListener { exception ->
                                Timber.e("get failed with $exception")
                            }
                    }
                } else {
                    Timber.d("No such document")
                }
            }
            .addOnFailureListener { }
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

    private fun loadMoreData() {
        Timber.d("Load more data")
        lastSnapshot?.let {
            val userInfo = userInfo
            if(userInfo != null) {
                when (page) {
                    PAGE_MINE -> firestoreHandler.loadQuestsMineMore(userInfo, it)
                    PAGE_FOLLOWING -> firestoreHandler.loadQuestsFollowingMore(userInfo, it)
                    PAGE_ALL -> firestoreHandler.loadQuestsAllMore(it)
                    else -> null
                }?.addOnSuccessListener { collection ->
                    if (collection != null && !collection.isEmpty) {
                        lastSnapshot = collection.documents.last()
                        Timber.d("DocumentSnapshot data: ${collection.documents}")
                        processQuests(collection)
                    } else {
                        Timber.d("No such document")
                    }
                }?.addOnFailureListener { exception ->
                    Timber.e("get failed with $exception")
                }
            }
        }
    }

    fun onReachedItem(position: Int) {
        Timber.d("Reached position $position")
        Timber.d("Items size ${allQuests.size}")
        if (position == allQuests.size - 1) {
            loadMoreData()
        }
    }
}