package xevenition.com.runage.fragment.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.LevelCalculator
import xevenition.com.runage.util.ResourceUtil

class ProfileViewModel(
    gameServicesUtil: GameServicesUtil,
    resourceUtil: ResourceUtil,
    userRepository: UserRepository
) : BaseViewModel() {

    private val _liveTextName = MutableLiveData<String>()
    val liveTextName: LiveData<String> = _liveTextName

    private val _observableProfileImage = MutableLiveData<Uri>()
    val observableProfileImage: LiveData<Uri> = _observableProfileImage

    private val _liveLevelText = MutableLiveData<String>()
    val liveLevelText: LiveData<String> = _liveLevelText

    init {
        val disposable = userRepository.getObservableUser()
            .subscribe({
                Timber.d("Got user info")
                val level = LevelCalculator.getLevel(it?.xp ?: 0)
                _liveLevelText.postValue("${resourceUtil.getString(R.string.runage_level)} $level")
            }, {
                Timber.e("Failed to get the user")
            })
        addDisposable(disposable)
    }

}
