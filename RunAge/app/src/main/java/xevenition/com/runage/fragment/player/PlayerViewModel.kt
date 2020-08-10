package xevenition.com.runage.fragment.player

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.feed.FeedFragmentDirections
import xevenition.com.runage.fragment.start.StartFragmentDirections
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.repository.UserRepository
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

    private val _liveTextWin = MutableLiveData<String>()
    val liveTextWin: LiveData<String> = _liveTextWin

    private val _liveTextPenalty = MutableLiveData<String>()
    val liveTextPenalty: LiveData<String> = _liveTextPenalty

    private val _liveFollowButtonColor = MutableLiveData<Int>()
    val liveFollowButtonColor: LiveData<Int> = _liveFollowButtonColor

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
            if (quest.playerName.isNullOrEmpty()) {
                resourceUtil.getString(R.string.runage_unknown_player)
            } else {
                quest.playerName
            }
        )

        _liveTextWin.postValue("+" + (quest.xp / 2).toString() + " " + resourceUtil.getString(R.string.runage_xp))
        _liveTextPenalty.postValue("-" + (quest.xp / 4).toString() + " " + resourceUtil.getString(R.string.runage_xp))

        val userToFollowId = quest.userId
        val disposable = userRepository.getObservableUser()
            .subscribe({
                if (it.following.contains(userToFollowId)) {
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_unfollow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_cancel))
                    _liveFollowButtonColor.postValue(resourceUtil.getColor(R.color.red))
                } else {
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_follow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_follow_single))
                    _liveFollowButtonColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
                }
            }, {
                Timber.e(it)
            })
        addDisposable(disposable)
    }

    @SuppressLint("CheckResult")
    fun onFollowClicked() {
        val userToFollowId = quest.userId
        userRepository.getSingleUser()
            .subscribe({ user ->
                if (user.following.contains(userToFollowId)) {
                    val newList = user.following.toMutableList()
                    newList.remove(userToFollowId)
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_follow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_follow_single))
                    _liveFollowButtonColor.postValue(resourceUtil.getColor(R.color.colorPrimary))

                    val newUserInfo = setUpNewUser(user, newList)
                    userRepository.removeUserFollowing(newUserInfo, userToFollowId)
                        .subscribe({
                            Timber.d("User following status changed")
                        }, {
                            Timber.e(it)
                        })
                } else {
                    val newList = user.following.toMutableList()
                    newList.add(userToFollowId)
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_unfollow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_cancel))
                    _liveFollowButtonColor.postValue(resourceUtil.getColor(R.color.red))
                    val newUserInfo = setUpNewUser(user, newList)
                    userRepository.addUserFollowing(newUserInfo, userToFollowId)
                        .subscribe({
                            Timber.d("User following status changed")
                        }, {
                            Timber.e(it)
                        })
                }
            }, {
                Timber.e(it)
            })
    }

    private fun setUpNewUser(
        user: RunageUser,
        newList: MutableList<String>
    ): RunageUser {
        return RunageUser(
            userId = user.userId,
            xp = user.xp,
            calories = user.calories,
            distance = user.distance,
            challengeScore = user.challengeScore,
            following = newList,
            followers = user.followers,
            playerName = user.playerName,
            playerChallengesLost = user.playerChallengesLost,
            playerChallengesWon = user.playerChallengesWon,
            completedRuns = user.completedRuns,
            duration = user.duration
        )
    }

    fun onViewProfileClicked() {
        observableNavigateTo.postValue(
            PlayerFragmentDirections.actionPlayerFragmentToProfileFragment(
                keyUserId = quest.userId,
                keyIsUser = false
            )
        )
    }

    fun onStartClicked() {
        val duration = quest.endTimeEpochSeconds - quest.startTimeEpochSeconds
        val challenge = Challenge(
            level = 0,
            distance = quest.totalDistance,
            time = duration.toInt(),
            experience = quest.xp / 2,
            isPlayerChallenge = true
        )
        observableNavigateTo.postValue(
            PlayerFragmentDirections.actionPlayerFragmentToMapFragment(
                keyChallenge = challenge
            )
        )
    }

}
