package xevenition.com.runage.fragment.player

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.room.repository.UserRepository
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil

class PlayerViewModel(
    gameServicesUtil: GameServicesUtil,
    private val resourceUtil: ResourceUtil,
    runningUtil: RunningUtil,
    private val userRepository: UserRepository,
    playerFragmentArgs: PlayerFragmentArgs
) : BaseViewModel() {

    val quest = playerFragmentArgs.keySavedQuest

    private val _liveTextName = MutableLiveData<String>()
    val liveTextName: LiveData<String> = _liveTextName

    private val _liveLevelProgress = MutableLiveData<Float>()
    val liveLevelProgress: LiveData<Float> = _liveLevelProgress

    private val _liveLevelNext = MutableLiveData<Float>()
    val liveLevelNext: LiveData<Float> = _liveLevelNext

    private val _liveTextTime = MutableLiveData<String>()
    val liveTextTime: LiveData<String> = _liveTextTime

    private val _liveTextDistance = MutableLiveData<String>()
    val liveTextDistance: LiveData<String> = _liveTextDistance

    private val _liveFollowText = MutableLiveData<String>()
    val liveFollowText: LiveData<String> = _liveFollowText

    private val _liveFollowIcon = MutableLiveData<Drawable>()
    val liveFollowIcon: LiveData<Drawable> = _liveFollowIcon

    private val _observableImage = MutableLiveData<String>()
    val observableImage: LiveData<String> = _observableImage

    init {
        _observableImage.postValue(quest.userId)
        val duration = quest.endTimeEpochSeconds - quest.startTimeEpochSeconds
        _liveTextTime.postValue(runningUtil.convertTimeToDurationString(duration))
        _liveTextDistance.postValue(runningUtil.getDistanceString(quest.totalDistance))
        _liveTextName.postValue(
            if (quest.playerName.isEmpty()) {
                resourceUtil.getString(R.string.runage_unknown_player)
            } else {
                quest.playerName
            }
        )

        val userToFollowId = quest.userId
        userRepository.getSingleUser()
            .subscribe({
                if(it.following.contains(userToFollowId)){
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_unfollow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_cancel))
                }else{
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_follow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_follow_single))
                }
            },{
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    fun onFollowClicked() {
        val userToFollowId = quest.userId
        userRepository.getSingleUser()
            .subscribe({
                val newList = if(it.following.contains(userToFollowId)){
                    val newList = it.following.toMutableList()
                    newList.remove(userToFollowId)
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_follow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_follow_single))
                    newList
                }else{
                    val newList = it.following.toMutableList()
                    newList.add(userToFollowId)
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_unfollow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_cancel))
                    newList
                }
                val newUserInfo = RunageUser(
                    userId = it.userId,
                    xp = it.xp,
                    calories = it.calories,
                    distance = it.distance,
                    challengeScore = it.challengeScore,
                    following = newList,
                    duration = it.duration
                )
                userRepository.updateFollowing(newUserInfo)
                    .subscribe({
                        Timber.d("User following status changed")
                    },{
                        Timber.e(it)
                    })
            },{
                Timber.e(it)
            })
    }

}
