package xevenition.com.runage.fragment.profile

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
    resourceUtil: ResourceUtil,
    userRepository: UserRepository,
    args: ProfileFragmentArgs
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

    init {
        _observableProfileImage.postValue(args.keyUserId)
        userRepository.getUserInfo(args.keyUserId)
            .addOnSuccessListener {
                val userInfo = it.toObject(RunageUser::class.java)
                val level = LevelCalculator.getLevel(userInfo?.xp ?: 0)
                _liveLevelText.postValue("${resourceUtil.getString(R.string.runage_level)} $level")
                _liveTextName.postValue(userInfo?.playerName)
                _liveTextDistance.postValue(runningUtil.getDistanceString(userInfo?.distance ?: 0))
                _liveTextDistance.postValue(runningUtil.convertTimeToDurationString(userInfo?.duration?.toLong() ?: 0))
                _liveTextExperience.postValue(userInfo?.xp?.toString() ?: "0")

            }
            .addOnFailureListener { }
    }

}
