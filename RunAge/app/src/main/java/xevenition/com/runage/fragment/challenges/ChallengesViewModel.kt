package xevenition.com.runage.fragment.challenges

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.ResourceUtil

class ChallengesViewModel(
    resourceUtil: ResourceUtil,
    userRepository: UserRepository
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

        val disposable = userRepository.getObservableUser()
            .subscribe({
                challengeScores = it?.challengeScore
                Timber.d("Got user info")
                val challengeJson = resourceUtil.getString(R.string.runage_quests_json)
                processChallenges(challengeJson)
            },{
                Timber.e(it)
            })
        addDisposable(disposable)
    }

    @SuppressLint("CheckResult")
    private fun processChallenges(json: String) {
        Observable.just(json)
            .subscribeOn(Schedulers.computation())
            .map {
                val itemType = object : TypeToken<List<Challenge>>() {}.type
                Gson().fromJson<List<Challenge>>(it, itemType)
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
            QuestsFragmentDirections.actionChallengeFragmentToRequirementFragment(challengeScores?.getOrDefault(challenge.level.toString(), 0) ?: 0, challenge)
        )
    }
}