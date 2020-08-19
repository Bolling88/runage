package xevenition.com.runage.fragment.start

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import timber.log.Timber
import xevenition.com.runage.MainApplication.Companion.serviceIsRunning
import xevenition.com.runage.MainApplication.Companion.welcomeMessagePlayed
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.service.GameServicesService
import xevenition.com.runage.util.*
import java.io.ByteArrayOutputStream
import java.time.Instant

class StartViewModel(
    private val gameServicesUtil: GameServicesUtil,
    private val gameServicesService: GameServicesService,
    private val resourceUtil: ResourceUtil,
    private val feedbackHandler: FeedbackHandler,
    private val saveUtil: SaveUtil,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private var level: Int = 0
    private var userInfo: RunageUser? = null
    private var profileImageUploaded = false
    private var storageRef = Firebase.storage.reference

    private val _liveTextName = MutableLiveData<String>()
    val liveTextName: LiveData<String> = _liveTextName

    private val _observableLevelProgress = MutableLiveData<Float>()
    val observableLevelProgress: LiveData<Float> = _observableLevelProgress

    private val _liveLevelNext = MutableLiveData<Float>()
    val liveLevelNext: LiveData<Float> = _liveLevelNext

    private val _liveLevelText = MutableLiveData<String>()
    val liveLevelText: LiveData<String> = _liveLevelText

    private val _liveTextXp = MutableLiveData<String>()
    val liveTextXp: LiveData<String> = _liveTextXp

    private val _livePresentVisibility = MutableLiveData<Int>()
    val livePresentVisibility: LiveData<Int> = _livePresentVisibility

    val observableOpenMenu = SingleLiveEvent<Unit>()
    val observableShowProfile = SingleLiveEvent<Bundle>()

    private val _observableProfileImage = MutableLiveData<Uri>()
    val observableProfileImage: LiveData<Uri> = _observableProfileImage

    val observableShowAchievements = SingleLiveEvent<Intent>()
    val observableShowRateDialog = SingleLiveEvent<Unit>()
    val observableShowLevelUp = SingleLiveEvent<Unit>()

    init {
        if (serviceIsRunning) {
            observableNavigateTo.postValue(StartFragmentDirections.actionStartFragmentToMapFragment())
        }

        _livePresentVisibility.postValue(View.GONE)

        val openedApp = saveUtil.getInt(SaveUtil.KEY_APP_OPENINGS, 0)
        val rated = saveUtil.getBoolean(SaveUtil.KEY_RATED, false)
        if (openedApp >= NUMBER_OF_APP_OPENINGS && !rated) {
            observableShowRateDialog.call()
        }

        userRepository.refreshUserInfo()
            .subscribe()

        val disposable = userRepository.getObservableUser()
            .subscribe({
                Timber.d("Got user info")
                userInfo = it
                level = LevelCalculator.getLevel(userInfo?.xp ?: 0)
                val xpPreviousLevel = LevelCalculator.getXpForLevel(level)
                val xpNextLevel = LevelCalculator.getXpForLevel(level + 1)
                val totalXpForNextLevel = xpNextLevel - xpPreviousLevel
                val userXp = userInfo?.xp ?: 0
                val progress = userXp - xpPreviousLevel
                _liveTextXp.postValue("$progress / $totalXpForNextLevel")
                _liveLevelNext.postValue(totalXpForNextLevel.toFloat())
                _observableLevelProgress.postValue(progress.toFloat())
                _liveLevelText.postValue("${resourceUtil.getString(R.string.runage_level)} $level")
                _liveTextName.postValue(it.playerName)

                gameServicesUtil.saveLeaderBoard(
                    resourceUtil.getString(R.string.leaderboard_most_experience),
                    userXp.toLong()
                )
                gameServicesUtil.saveLeaderBoard(
                    resourceUtil.getString(R.string.leaderboard_highest_level),
                    level.toLong()
                )
            }, {
                Timber.e("Failed to get the user")
            })
        addDisposable(disposable)
    }

    fun onViewResumed() {
        val dateClaimed = saveUtil.getLong(SaveUtil.KEY_REWARD_CLAIMED_DATE, 0)
        val dateNow = Instant.now().epochSecond
        if (dateNow - dateClaimed > SECONDS_24_HOURS) {
            _livePresentVisibility.postValue(View.VISIBLE)
        } else {
            _livePresentVisibility.postValue(View.GONE)
        }

        getGameServicesInfo()
    }

    private fun getGameServicesInfo() {
        val task = gameServicesService.getGamesPlayerInfo()
        task?.addOnSuccessListener {
            if (!serviceIsRunning && !welcomeMessagePlayed) {
                if (level > saveUtil.getInt(SaveUtil.KEY_CURRENT_LEVEL, 0)) {
                    feedbackHandler.speak(resourceUtil.getString(R.string.runage_congrats_level_up) + " " + level)
                    observableShowLevelUp.call()
                    saveUtil.saveInt(SaveUtil.KEY_CURRENT_LEVEL, level)
                } else {
                    feedbackHandler.speak(
                        "${resourceUtil.getString(R.string.runage_welcome_back)} ${it.displayName}",
                        TextToSpeech.QUEUE_FLUSH
                    )
                }
                userRepository.updateGameServicesInfo(it.displayName, it.playerId)
                welcomeMessagePlayed = true
            } else if (!serviceIsRunning && level > saveUtil.getInt(SaveUtil.KEY_CURRENT_LEVEL, 0)) {
                feedbackHandler.speak(resourceUtil.getString(R.string.runage_congrats_level_up) + " " + level)
                observableShowLevelUp.call()
                saveUtil.saveInt(SaveUtil.KEY_CURRENT_LEVEL, level)
            }
            _observableProfileImage.postValue(it.hiResImageUri)
            _liveTextName.postValue(it.displayName)
        }
    }

    fun onStartClicked() {
        observableNavigateTo.postValue(StartFragmentDirections.actionStartFragmentToMapFragment())
    }

    fun onProfileClicked() {
        val level = LevelCalculator.getLevel(userInfo?.xp ?: 0)
        val levelString = "${resourceUtil.getString(R.string.runage_level)} $level"
        val result = Bundle()
        result.putString("key_user_id", userInfo?.userId ?: "")
        result.putBoolean("key_is_user", true)
        result.putString("key_user_name", userInfo?.playerName ?: "")
        result.putString("key_user_level", levelString)
        observableShowProfile.postValue(result)
    }

    fun onPresentClicked() {
        observableNavigateTo.postValue(StartFragmentDirections.actionStartFragmentToPresentFragment())
    }

    fun onRateLaterClicked() {
        saveUtil.saveInt(SaveUtil.KEY_APP_OPENINGS, 0)
        saveUtil.saveBoolean(SaveUtil.KEY_RATED, false)
    }

    fun onRateClicked() {
        saveUtil.saveBoolean(SaveUtil.KEY_RATED, true)
    }

    fun onDislikeClicked() {
        saveUtil.saveBoolean(SaveUtil.KEY_RATED, true)
    }

    fun onProfileImageRetrieved(bitmap: Bitmap?) {
        //We need to update it every time, because we don't know when it could have changed
        if (bitmap != null && !profileImageUploaded) {
            Timber.d("Got profile image bitmap")
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val profileImageRef = storageRef.child("images/${userInfo?.userId}.jpg")
            val uploadTask = profileImageRef.putBytes(data)
            uploadTask.addOnFailureListener {
                Timber.e("Profile image upload failed")
            }.addOnSuccessListener {
                Timber.d("Profile image uploaded")
                profileImageUploaded = true
            }
        } else {
            Timber.e("Profile image already uploaded")
        }
    }

    companion object {
        const val NUMBER_OF_APP_OPENINGS = 10
        const val SECONDS_24_HOURS = 86400
    }
}
