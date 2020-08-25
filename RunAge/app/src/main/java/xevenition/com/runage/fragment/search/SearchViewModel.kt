package xevenition.com.runage.fragment.search

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.FeedItem
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.model.SearchItem
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.util.ResourceUtil

class SearchViewModel(
    private val resourceUtil: ResourceUtil,
    private val userRepository: UserRepository,
    private val firestoreService: FireStoreService
) : BaseViewModel() {

    private var ads: MutableList<UnifiedNativeAd> = mutableListOf()
    private var users: MutableList<RunageUser> = mutableListOf()
    private var userInfo: RunageUser? = null
    private var lastSnapshot: DocumentSnapshot? = null
    private var allFeedItems = mutableListOf<SearchItem>()

    private val _observableUsers = MutableLiveData<List<FeedItem>>()
    val observableUsers: LiveData<List<FeedItem>> = _observableUsers

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    private val _liveEmptyText = MutableLiveData<String>()
    val liveEmptyText: LiveData<String> = _liveEmptyText

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)

        _liveEmptyText.postValue(resourceUtil.getString(R.string.runage_no_completed_runs))

        val disposable = userRepository.getObservableUser()
            .subscribe({ user ->
                allFeedItems.clear()
                userInfo = user
                userInfo?.let {
                    firestoreService.loadQuestsMine(it)
                        .addOnSuccessListener { collection ->
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
                        .addOnFailureListener { exception ->
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

    private fun loadMoreData() {
        Timber.d("Load more data")
        lastSnapshot?.let {
            val user = userInfo
            if (user != null) {
                firestoreService.loadQuestsMineMore(user, it)
                    ?.addOnSuccessListener { collection ->
                        if (collection != null && !collection.isEmpty) {
                            lastSnapshot = collection.documents.last()
                            Timber.d("DocumentSnapshot data: ${collection.documents}")
                            processQuests(collection)
                        } else {
                            Timber.d("No such document")
                        }
                    }.addOnFailureListener { exception ->
                        Timber.e("get failed with $exception")
                    }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun processQuests(collection: QuerySnapshot) {
        Observable.fromIterable(collection)
            .subscribeOn(Schedulers.computation())
            .map {
                val quest = it.toObject(SavedQuest::class.java)
                val idQuest = quest.copy(questId = it.id)
                //users.add(idQuest)
                users
            }
            .subscribe({
                combineQuestsWithAds()
            }, {
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    private fun combineQuestsWithAds() {
        Observable.just(users)
            .subscribeOn(Schedulers.computation())
            .map {
                val feedList = mutableListOf<FeedItem>()
                var adCounter = 0
                for ((counter, quest) in it.withIndex()) {
                    //feedList.add(SearchItem(savedQuest = quest, ad = null))
                    if (counter % 3 == 0 && ads.isNotEmpty()) {
                        if (adCounter < ads.size) {
                            feedList.add(FeedItem(savedQuest = null, ad = ads[adCounter]))
                            adCounter++
                        } else {
                            feedList.add(FeedItem(savedQuest = null, ad = ads[0]))
                            adCounter = 0
                        }
                    }
                }
                feedList
            }
            .subscribe({
                Timber.d("Quests processed")
                _observableUsers.postValue(it)
                //allFeedItems = it.toMutableList()
                _liveProgressVisibility.postValue(View.GONE)

                if (allFeedItems.isEmpty()) {
                    _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                } else {
                    _liveNoRunsTextVisibility.postValue(View.GONE)
                }
            }, {
                Timber.e(it)
            })
    }

    fun insertAdsInMenuItems(mNativeAds: MutableList<UnifiedNativeAd>) {
        ads = mNativeAds
        combineQuestsWithAds()
    }

    fun onReachedItem(position: Int) {
        Timber.d("Reached position $position")
        Timber.d("Items size ${allFeedItems.size}")
        if (position == allFeedItems.size - 1) {
            loadMoreData()
        }
    }
}