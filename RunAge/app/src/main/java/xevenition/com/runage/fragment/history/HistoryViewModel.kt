package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.KEY_PAGE
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_ALL
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_FOLLOWING
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_MINE
import xevenition.com.runage.model.FeedItem
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.SingleLiveEvent


class HistoryViewModel(
    resourceUtil: ResourceUtil,
    private val firestoreService: FireStoreService,
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    args: Bundle
) : BaseViewModel() {

    private var ads: MutableList<UnifiedNativeAd> = mutableListOf()
    private var quests: MutableList<SavedQuest> = mutableListOf()
    private var userInfo: RunageUser? = null
    private var lastSnapshot: DocumentSnapshot? = null
    private var allFeedItems = mutableListOf<FeedItem>()

    private val _observableQuests = MutableLiveData<List<FeedItem>>()
    val observableQuest: LiveData<List<FeedItem>> = _observableQuests

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    private val _liveEmptyText = MutableLiveData<String>()
    val liveEmptyText: LiveData<String> = _liveEmptyText

    val observableOpenPlayerSearch = SingleLiveEvent<Intent>()

    private val page = args.getInt(KEY_PAGE)

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)

        when(page){
            PAGE_MINE -> {
                _liveEmptyText.postValue(resourceUtil.getString(R.string.runage_no_completed_runs))
            }
            PAGE_FOLLOWING -> {
                _liveEmptyText.postValue(resourceUtil.getString(R.string.runage_you_are_not_following))
            }
            PAGE_ALL -> {
                _liveEmptyText.postValue(resourceUtil.getString(R.string.runage_unexpected_error))
            }
        }

        setUpObservableDeletedQuest()

        val disposable = userRepository.getObservableUser()
            .subscribe({ user ->
                allFeedItems.clear()
                userInfo = user
                userInfo?.let {
                    when (page) {
                        PAGE_MINE -> firestoreService.loadQuestsMine(it)
                        PAGE_FOLLOWING -> if (user.following.isEmpty()) {
                            _liveProgressVisibility.postValue(View.GONE)
                            _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                            null
                        } else firestoreService.loadQuestsFollowing(it)
                        PAGE_ALL -> firestoreService.loadQuestsAll()
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

    private fun loadMoreData() {
        Timber.d("Load more data")
        lastSnapshot?.let {
            val user = userInfo
            if (user != null) {
                when (page) {
                    PAGE_MINE -> firestoreService.loadQuestsMineMore(user, it)
                    PAGE_FOLLOWING -> if (user.following.isEmpty()) null else firestoreService.loadQuestsFollowingMore(
                        user,
                        it
                    )
                    PAGE_ALL -> firestoreService.loadQuestsAllMore(it)
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

    private fun setUpObservableDeletedQuest() {
        val disposable = questRepository.deletedSavedQuests.subscribeOn(Schedulers.io())
            .subscribe({
                val deleted = allFeedItems.removeAll { feedItem -> feedItem.savedQuest?.questId == it }
                if (deleted) {
                    Timber.d("Quest deleted")
                    _observableQuests.postValue(allFeedItems)
                } else {
                    Timber.d("Quest not found")
                }
            }, {
                Timber.e(it)
            })
        addDisposable(disposable)
    }

    @SuppressLint("CheckResult")
    private fun processQuests(collection: QuerySnapshot) {
        Observable.fromIterable(collection)
            .subscribeOn(Schedulers.computation())
            .map {
                val quest = it.toObject(SavedQuest::class.java)
                val idQuest = quest.copy(questId = it.id)
                quests.add(idQuest)
                quests
            }
            .subscribe({
                combineQuestsWithAds()
            }, {
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    private fun combineQuestsWithAds(){
        Observable.just(quests)
            .subscribeOn(Schedulers.computation())
            .map {
                val feedList = mutableListOf<FeedItem>()
                var adCounter = 0
                for((counter, quest) in it.withIndex()){
                    feedList.add(FeedItem(savedQuest = quest, ad = null))
                    if(counter % 3 == 0 && ads.isNotEmpty()){
                        if(adCounter < ads.size) {
                            feedList.add(FeedItem(savedQuest = null, ad = ads[adCounter]))
                            adCounter++
                        }else{
                            feedList.add(FeedItem(savedQuest = null, ad = ads[0]))
                            adCounter = 0
                        }
                    }
                }
                feedList
            }
            .subscribe({
                Timber.d("Quests processed")
                _observableQuests.postValue(it)
                allFeedItems = it.toMutableList()
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

    fun onSearchClicked(){
        userRepository.getSearchForUserIntent()
            ?.addOnSuccessListener {
                observableOpenPlayerSearch.postValue(it)
            }
    }
}