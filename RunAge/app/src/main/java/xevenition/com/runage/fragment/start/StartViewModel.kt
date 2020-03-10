package xevenition.com.runage.fragment.start

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.AccountUtil

class StartViewModel(accountUtil: AccountUtil) : BaseViewModel() {

    val liveTextName = MutableLiveData<String>()

    val observableProfileImage = MutableLiveData<Uri>()

    init {
        val task = accountUtil.getGamesPlayerInfo()
        task?.addOnSuccessListener {
            liveTextName.postValue(it.displayName)
            observableProfileImage.postValue(it.iconImageUri)
        }
    }
}
