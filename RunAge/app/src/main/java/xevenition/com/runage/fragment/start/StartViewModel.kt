package xevenition.com.runage.fragment.start

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import xevenition.com.runage.util.SingleLiveEvent
import timber.log.Timber
import xevenition.com.runage.MainApplication.Companion.serviceIsRunning
import xevenition.com.runage.MainApplication.Companion.welcomeMessagePlayed
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.*
import java.io.ByteArrayOutputStream

class StartViewModel(
    private val gameServicesUtil: GameServicesUtil,
    private val gameServicesService: GameServicesService,
    private val resourceUtil: ResourceUtil,
    private val feedbackHandler: FeedbackHandler,
    private val saveUtil: SaveUtil,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private var userInfo: RunageUser? = null
    private var profileImageUploaded = false
    private var storageRef = Firebase.storage.reference

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
    val observableShowRateDialog = SingleLiveEvent<Unit>()

    init {
        if(serviceIsRunning){
            observableNavigateTo.postValue(StartFragmentDirections.actionStartFragmentToMapFragment())
        }

        val openedApp = saveUtil.getInt(SaveUtil.KEY_APP_OPENINGS, 0)
        val rated = saveUtil.getBoolean(SaveUtil.KEY_RATED, false)
        if(openedApp >= NUMBER_OF_APP_OPENINGS && !rated){
            observableShowRateDialog.call()
        }

        userRepository.refreshUserInfo()
            .subscribe()

        getGameServicesInfo()

        val disposable = userRepository.getObservableUser()
            .subscribe({
                Timber.d("Got user info")
                userInfo = it
                val level = LevelCalculator.getLevel(userInfo?.xp ?: 0)
                val xpPreviousLevel = LevelCalculator.getXpForLevel(level)
                val xpNextLevel = LevelCalculator.getXpForLevel(level + 1)
                val totalXpForNextLevel = xpNextLevel - xpPreviousLevel
                val userXp = userInfo?.xp ?: 0
                val progress = userXp - xpPreviousLevel
                _liveTextXp.postValue("$progress / $totalXpForNextLevel")
                _liveLevelNext.postValue(totalXpForNextLevel.toFloat())
                _liveLevelProgress.postValue(progress.toFloat())
                _liveLevelText.postValue("${resourceUtil.getString(R.string.runage_level)} $level")
                _liveTextName.postValue(it.playerName)

                gameServicesUtil.saveLeaderBoard(resourceUtil.getString(R.string.leaderboard_most_experience), userXp.toLong())
                gameServicesUtil.saveLeaderBoard(resourceUtil.getString(R.string.leaderboard_highest_level), level.toLong())
            },{
                Timber.e("Failed to get the user")
            })
        addDisposable(disposable)
    }

    private fun getGameServicesInfo() {
        val task = gameServicesService.getGamesPlayerInfo()
        task?.addOnSuccessListener {
            if (!serviceIsRunning && !welcomeMessagePlayed) {
                feedbackHandler.speak(
                    "${resourceUtil.getString(R.string.runage_welcome_back)} ${it.displayName}",
                    TextToSpeech.QUEUE_FLUSH
                )
                welcomeMessagePlayed = true
            }
            _liveTextName.postValue(it.displayName)
            userRepository.updateUserName(it.displayName)

            _observableProfileImage.postValue(it.hiResImageUri)
        }
    }

    fun onStartClicked(){
        observableNavigateTo.postValue(StartFragmentDirections.actionStartFragmentToMapFragment())
    }

    fun onProfileClicked(){
//        accountUtil.getGamesPlayerInfo()?.addOnSuccessListener { player ->
//            accountUtil.getPlayerProfileIntent(player)?.addOnSuccessListener {
//                observableShowAchievements.postValue(it)
//            }
//        }
        observableNavigateTo.postValue(StartFragmentDirections.actionStartFragmentToProfileFragment(keyUserId = userInfo?.userId ?: "", keyIsUser = true))
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
        if(bitmap != null && !profileImageUploaded){
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
        }else{
            Timber.e("Profile image already uploaded")
        }
    }

    companion object{
        const val NUMBER_OF_APP_OPENINGS = 10
    }
}
