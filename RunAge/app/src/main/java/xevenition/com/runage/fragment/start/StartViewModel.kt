package xevenition.com.runage.fragment.start

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.MainApplication.Companion.serviceIsRunning
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.main.MainFragmentDirections
import xevenition.com.runage.service.EventService
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.ResourceUtil

class StartViewModel(
    accountUtil: AccountUtil,
    resourceUtil: ResourceUtil,
    feedbackHandler: FeedbackHandler
) : BaseViewModel() {

    val liveTextName = MutableLiveData<String>()

    private val _observableProfileImage = MutableLiveData<Uri>()
    val observableProfileImage: LiveData<Uri> = _observableProfileImage

    init {
        val task = accountUtil.getGamesPlayerInfo()
        task?.addOnSuccessListener {
            if (!serviceIsRunning) {
                feedbackHandler.speak("${resourceUtil.getString(R.string.runage_welcome_back)} ${it.displayName}")
            }
            liveTextName.postValue(it.displayName)
            _observableProfileImage.postValue(it.iconImageUri)
        }
    }

    fun onHistoryClicked(){
        observableNavigateTo.postValue(MainFragmentDirections.actionMainFragmentToHistoryFragment())
    }
}
