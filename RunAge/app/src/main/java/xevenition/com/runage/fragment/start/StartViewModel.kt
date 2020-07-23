package xevenition.com.runage.fragment.start

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import timber.log.Timber
import xevenition.com.runage.MainApplication.Companion.serviceIsRunning
import xevenition.com.runage.MainApplication.Companion.welcomeMessagePlayed
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.util.*

class StartViewModel(
    private val gameServicesUtil: GameServicesUtil,
    fireStoreHandler: FireStoreHandler,
    private val accountUtil: AccountUtil,
    resourceUtil: ResourceUtil,
    feedbackHandler: FeedbackHandler
) : BaseViewModel() {

    private val _liveTextName = MutableLiveData<String>()
    val liveTextName : LiveData<String> = _liveTextName

    private val _liveLevelProgress = MutableLiveData<Float>()
    val liveLevelProgress : LiveData<Float> = _liveLevelProgress

    private val _liveLevelNext = MutableLiveData<Float>()
    val liveLevelNext : LiveData<Float> = _liveLevelNext

    private val _liveLevelText = MutableLiveData<String>()
    val liveLevelText : LiveData<String> = _liveLevelText

    private val _liveTextXp = MutableLiveData<String>()
    val liveTextXp : LiveData<String> = _liveTextXp

    val observableOpenMenu = SingleLiveEvent<Unit>()

    private val _observableProfileImage = MutableLiveData<Uri>()
    val observableProfileImage: LiveData<Uri> = _observableProfileImage

    val observableShowAchievements = SingleLiveEvent<Intent>()

    init {
        val task = accountUtil.getGamesPlayerInfo()
        task?.addOnSuccessListener {
            if (!serviceIsRunning && !welcomeMessagePlayed) {
                feedbackHandler.speak("${resourceUtil.getString(R.string.runage_welcome_back)} ${it.displayName}")
                welcomeMessagePlayed = true
            }
            _liveTextName.postValue(it.displayName)
            _observableProfileImage.postValue(it.hiResImageUri)
        }

        fireStoreHandler.getUserInfo()
            .addOnSuccessListener {document ->
                if (document != null) {
                    val userInfo = document.toObject(UserInfo::class.java)
                    Timber.d("Got user info")
                    val level = LevelCalculator.getLevel(userInfo?.xp ?: 0)
                    val xpPreviousLevel = LevelCalculator.getXpForLevel(level)
                    val xpNextLevel = LevelCalculator.getXpForLevel(level + 1)
                    val totalXpForNextLevel = xpNextLevel - xpPreviousLevel
                    val userXp = userInfo?.xp ?: 0
                    val progress = userXp - xpPreviousLevel
                    _liveTextXp.postValue("$userXp / $xpNextLevel")
                    _liveLevelNext.postValue(totalXpForNextLevel.toFloat())
                    _liveLevelProgress.postValue(progress.toFloat())
                    _liveLevelText.postValue("${resourceUtil.getString(R.string.runage_level)} $level")

                    gameServicesUtil.saveLeaderBoard(resourceUtil.getString(R.string.leaderboard_most_experience), userXp.toLong())
                    gameServicesUtil.saveLeaderBoard(resourceUtil.getString(R.string.leaderboard_highest_level), level.toLong())
                } else {
                    Timber.d("No such document")
                }
            }
            .addOnFailureListener {  }
    }

    fun onStartClicked(){
        observableNavigateTo.postValue(StartFragmentDirections.actionStartFragmentToMapFragment())
    }

    fun onProfileClicked(){
        accountUtil.getGamesPlayerInfo()?.addOnSuccessListener { player ->
            accountUtil.getPlayerProfileIntent(player)?.addOnSuccessListener {
                observableShowAchievements.postValue(it)
            }
        }
    }
}
