package xevenition.com.runage.fragment.requirement

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.util.*

class RequirementViewModel(
    private val gameServicesUtil: GameServicesUtil,
    fireStoreHandler: FireStoreHandler,
    private val accountUtil: AccountUtil,
    private val runningUtil: RunningUtil,
    resourceUtil: ResourceUtil,
    feedbackHandler: FeedbackHandler,
    private val challenge: Challenge
) : BaseViewModel() {

    private val _liveTextDistance = MutableLiveData<String>()
    val liveTextDistance : LiveData<String> = _liveTextDistance

    private val _liveTextTime1 = MutableLiveData<String>()
    val liveTextTime1 : LiveData<String> = _liveTextTime1

    private val _liveTextTime2 = MutableLiveData<String>()
    val liveTextTime2 : LiveData<String> = _liveTextTime2

    private val _liveTextTime3 = MutableLiveData<String>()
    val liveTextTime3 : LiveData<String> = _liveTextTime3

    private val _liveTextXp = MutableLiveData<String>()
    val liveTextXp : LiveData<String> = _liveTextXp

    private val _liveTextLevel = MutableLiveData<String>()
    val liveTextLevel : LiveData<String> = _liveTextLevel

    val observableOpenMenu = SingleLiveEvent<Unit>()

    private val _observableProfileImage = MutableLiveData<Uri>()
    val observableProfileImage: LiveData<Uri> = _observableProfileImage

    val observableShowAchievements = SingleLiveEvent<Intent>()

    init {
        _liveTextLevel.postValue(challenge.level.toString())
        _liveTextTime1.postValue(runningUtil.convertTimeToDurationString(challenge.time.toLong()))
        _liveTextTime2.postValue(runningUtil.convertTimeToDurationString(challenge.time.toLong()-20))
        _liveTextTime3.postValue(runningUtil.convertTimeToDurationString(challenge.time.toLong()-40))
        _liveTextXp.postValue("${challenge.experience} ${resourceUtil.getString(R.string.runage_xp)}")
        _liveTextDistance.postValue(runningUtil.getDistanceString(challenge.distance))
    }

    fun onProfileClicked(){
        accountUtil.getGamesPlayerInfo()?.addOnSuccessListener { player ->
            accountUtil.getPlayerProfileIntent(player)?.addOnSuccessListener {
                observableShowAchievements.postValue(it)
            }
        }
    }
}
