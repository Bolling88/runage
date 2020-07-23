package xevenition.com.runage.fragment.quests

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.history.HistoryFragmentDirections
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.model.ChallengeData
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil

class QuestsViewModel(
    resourceUtil: ResourceUtil,
    fireStoreHandler: FireStoreHandler
) : BaseViewModel() {

    private var challengeScores: Map<String, Int>? = null
    private val _observableChallenges = MutableLiveData<Pair<List<Challenge>,Map<String, Int>?>>()
    val observableChallenges: LiveData<Pair<List<Challenge>,Map<String, Int>?>> = _observableChallenges

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)

        fireStoreHandler.getUserInfo()
            .addOnSuccessListener {document ->
                if (document != null) {
                    val userInfo = document.toObject(UserInfo::class.java)
                    challengeScores = userInfo?.challengeScore
                    Timber.d("Got user info")
                    fireStoreHandler.getChallenges()
                        .addOnSuccessListener {document ->
                            if (document != null) {
                                processChallenges(document)
                            } else {
                                Timber.d("No such document")
                            }
                        }
                        .addOnFailureListener {  }
                } else {
                    Timber.d("No such document")
                }
            }
            .addOnFailureListener {  }
    }

    @SuppressLint("CheckResult")
    private fun processChallenges(document: DocumentSnapshot) {
        Observable.just(document)
            .subscribeOn(Schedulers.computation())
            .map {
                document.toObject(ChallengeData::class.java)
            }
            .map {
                val itemType = object : TypeToken<List<Challenge>>() {}.type
                Gson().fromJson<List<Challenge>>(it.data, itemType)
            }
            .subscribe({
                Timber.d("Quests processed")
                _observableChallenges.postValue(Pair(it, challengeScores))
                _liveProgressVisibility.postValue(View.GONE)

                if (it.isEmpty()) {
                    _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                }
            }, {
                Timber.e(it)
            })
    }

    fun onChallengeClicked(challenge: Challenge) {
        observableNavigateTo.postValue(
            QuestsFragmentDirections.actionChallengeFragmentToViewPageFragment(true, challenge)
        )
    }
}