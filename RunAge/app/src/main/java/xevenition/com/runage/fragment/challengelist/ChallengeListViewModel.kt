package xevenition.com.runage.fragment.challengelist

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.model.ChallengeData
import xevenition.com.runage.util.FireStoreHandler
import xevenition.com.runage.util.ResourceUtil

class ChallengeListViewModel(
    resourceUtil: ResourceUtil,
    firestoreHandler: FireStoreHandler
) : BaseViewModel() {

    private val _observableChallenges = MutableLiveData<List<Challenge>>()
    val observableChallenges: LiveData<List<Challenge>> = _observableChallenges

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    init {
        _liveProgressVisibility.postValue(View.VISIBLE)
        _liveNoRunsTextVisibility.postValue(View.GONE)

        firestoreHandler.getChallenges()
            .addOnSuccessListener {document ->
                if (document != null) {
                    processChallenges(document)
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
                _observableChallenges.postValue(it)
                _liveProgressVisibility.postValue(View.GONE)

                if (it.isEmpty()) {
                    _liveNoRunsTextVisibility.postValue(View.VISIBLE)
                }
            }, {
                Timber.e(it)
            })
    }

    fun onChallengeClicked(challenge: Challenge) {

    }
}