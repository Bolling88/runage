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
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.room.repository.UserRepository
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil

class HistoryViewModel(
    resourceUtil: ResourceUtil,
    private val firestoreHandler: FireStoreHandler,
    private val userRepository: UserRepository,
    args: Bundle
) : BaseViewModel() {

    private var userInfo: RunageUser? = null
    private var lastSnapshot: DocumentSnapshot? = null
    private var allQuests = mutableListOf<SavedQuest>()

    private val _observableQuests = MutableLiveData<List<SavedQuest>>()
    val observableQuest: LiveData<List<SavedQuest>> = _observableQuests

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    private val _liveEmptyText = MutableLiveData<String>()
    val liveEmptyText: LiveData<String> = _liveEmptyText

    private val page = args.getInt(KEY_PAGE)

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)

        when(page){
            PAGE_MINE->{
                _liveEmptyText.postValue(resourceUtil.getString(R.string.runage_no_completed_runs))
            }
            PAGE_FOLLOWING->{
                _liveEmptyText.postValue(resourceUtil.getString(R.string.runage_you_are_not_following))
            }
            PAGE_ALL->{
                _liveEmptyText.postValue(resourceUtil.getString(R.string.runage_unexpected_error))
            }
        }

        val disposable = userRepository.getSingleUser()
            .subscribe({ user ->
                userInfo = user
                userInfo?.let {
                    when (page) {
                        PAGE_MINE -> firestoreHandler.loadQuestsMine(it)
                        PAGE_FOLLOWING -> if (user.following.isEmpty()) {
                            _liveProgressVisibility.postValue(View.GONE)
                            _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                            null
                        } else firestoreHandler.loadQuestsFollowing(it)
                        PAGE_ALL -> firestoreHandler.loadQuestsAll()
                        else -> null
                    }
                        ?.addOnSuccessListener { collection ->
                            if (collection != null && !collection.isEmpty) {
                                lastSnapshot = collection.documents.last()
                                Timber.d("DocumentSnapshot data: ${collection.documents}")
                                processQuests(collection)
                            } else {
                                Timber.d("No such document")
                                _liveProgressVisibility.postValue(View.GONE)
                                _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                            }
                        }
                        ?.addOnFailureListener { exception ->
                            Timber.e("get failed with $exception")
                            _liveProgressVisibility.postValue(View.GONE)
                            _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                        }
                }
            }, {
                Timber.e(it)
            })
        addDisposable(disposable)
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
            val user = userInfo
            if (user != null) {
                when (page) {
                    PAGE_MINE -> firestoreHandler.loadQuestsMineMore(user, it)
                    PAGE_FOLLOWING -> if (user.following.isEmpty()) null else firestoreHandler.loadQuestsFollowingMore(
                        user,
                        it
                    )
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