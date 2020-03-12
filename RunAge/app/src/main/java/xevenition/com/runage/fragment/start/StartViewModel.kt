package xevenition.com.runage.fragment.start

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.AccountUtil
import xevenition.com.runage.util.FeedbackHandler
import xevenition.com.runage.util.ResourceUtil

class StartViewModel(
    accountUtil: AccountUtil,
    resourceUtil: ResourceUtil,
    feedbackHandler: FeedbackHandler
) : BaseViewModel() {

    val liveTextName = MutableLiveData<String>()

    val observableProfileImage = MutableLiveData<Uri>()

    init {
        val task = accountUtil.getGamesPlayerInfo()
        task?.addOnSuccessListener {
            feedbackHandler.speak("${resourceUtil.getString(R.string.runage_welcome_back)} ${it.displayName}")
            liveTextName.postValue(it.displayName)
            observableProfileImage.postValue(it.iconImageUri)
        }
    }
}
