package xevenition.com.runage.fragment.profile

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.LevelCalculator
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil

class ProfileViewModel(
    runningUtil: RunningUtil,
    private val resourceUtil: ResourceUtil,
    private val userRepository: UserRepository,
    private val args: ProfileFragmentArgs
) : BaseViewModel() {

    private val _liveTextName = MutableLiveData<String>()
    val liveTextName: LiveData<String> = _liveTextName

    private val _observableProfileImage = MutableLiveData<String>()
    val observableProfileImage: LiveData<String> = _observableProfileImage

    private val _liveLevelText = MutableLiveData<String>()
    val liveLevelText: LiveData<String> = _liveLevelText

    private val _liveTextDistance = MutableLiveData<String>()
    val liveTextDistance: LiveData<String> = _liveTextDistance

    private val _liveTextExperience = MutableLiveData<String>()
    val liveTextExperience: LiveData<String> = _liveTextExperience

    private val _liveTextDuration = MutableLiveData<String>()
    val liveTextDuration: LiveData<String> = _liveTextDuration

    private val _liveTextFollowers = MutableLiveData<String>()
    val liveTextFollowers: LiveData<String> = _liveTextFollowers

    private val _liveTextTotalRuns = MutableLiveData<String>()
    val liveTextTotalRuns: LiveData<String> = _liveTextTotalRuns

    private val _liveFollowButtonColor = MutableLiveData<Int>()
    val liveFollowButtonColor: LiveData<Int> = _liveFollowButtonColor

    private val _liveFollowIcon = MutableLiveData<Drawable>()
    val liveFollowIcon: LiveData<Drawable> = _liveFollowIcon

    private val _liveFollowButtonTextColor = MutableLiveData<Int>()
    val liveFollowButtonTextColor: LiveData<Int> = _liveFollowButtonTextColor

    private val _liveFollowText = MutableLiveData<String>()
    val liveFollowText: LiveData<String> = _liveFollowText

    private val _liveTextStars = MutableLiveData<String>()
    val liveTextStars: LiveData<String> = _liveTextStars

    init {
        _observableProfileImage.postValue(args.keyUserId)
        userRepository.getUserInfo(args.keyUserId)
            .addOnSuccessListener {
                val userInfo = it.toObject(RunageUser::class.java)
                val level = LevelCalculator.getLevel(userInfo?.xp ?: 0)
                _liveLevelText.postValue("${resourceUtil.getString(R.string.runage_level)} $level")
                _liveTextName.postValue(userInfo?.playerName)
                _liveTextDistance.postValue(runningUtil.getDistanceString(userInfo?.distance ?: 0))
                _liveTextDuration.postValue(runningUtil.convertTimeToDurationString(userInfo?.duration?.toLong() ?: 0))
                _liveTextExperience.postValue(userInfo?.xp?.toString() ?: "0")
                _liveTextFollowers.postValue(userInfo?.followers?.size?.toString() ?: "0")
                _liveTextTotalRuns.postValue(userInfo?.completedRuns?.toString() ?: "0")

                var totalStars = 0
                for ((key, value) in userInfo?.challengeScore?: mapOf()) {
                    totalStars+= value
                }
                _liveTextStars.postValue(totalStars.toString())

            }
            .addOnFailureListener { }

        val disposable = userRepository.getSingleUser()
            .subscribe({
                if (it.following.contains(args.keyUserId)) {
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_unfollow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_cancel))
                    _liveFollowButtonColor.postValue(resourceUtil.getColor(R.color.red))
                    _liveFollowButtonTextColor.postValue(resourceUtil.getColor(R.color.white))
                } else {
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_follow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_follow_single))
                    _liveFollowButtonColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
                    _liveFollowButtonTextColor.postValue(resourceUtil.getColor(R.color.white))
                }
            }, {
                Timber.e(it)
            })
        addDisposable(disposable)
    }

    @SuppressLint("CheckResult")
    fun onFollowClicked(){
        val userToFollowId = args.keyUserId
        userRepository.getSingleUser()
            .subscribe({ user ->
                if (user.following.contains(userToFollowId)) {
                    val newList = user.following.toMutableList()
                    newList.remove(userToFollowId)
                    _liveFollowText.postValue(resourceUtil.getString(R.string.runage_follow_player))
                    _liveFollowIcon.postValue(resourceUtil.getDrawable(R.drawable.ic_follow_single))
                    _liveFollowButtonColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
                    _liveFollowButtonTextColor.postValue(resourceUtil.getColor(R.color.white))

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
                    _liveFollowButtonTextColor.postValue(resourceUtil.getColor(R.color.white))
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


}
