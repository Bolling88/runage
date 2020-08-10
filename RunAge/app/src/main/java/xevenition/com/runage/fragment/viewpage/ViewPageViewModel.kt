package xevenition.com.runage.fragment.viewpage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.util.GameServicesService

class ViewPageViewModel(private  val questRepository: QuestRepository,
                        private val gameServicesService: GameServicesService
) : BaseViewModel() {

    private val _observableUserAccount = MutableLiveData<GoogleSignInAccount>()
    val observableUserAccount: LiveData<GoogleSignInAccount> = _observableUserAccount

    fun onResume() {
        if(!gameServicesService.isAccountActive() || gameServicesService.getGamesAccount() == null){
            observableBackNavigation.call()
        }else{
            _observableUserAccount.postValue(gameServicesService.getGamesAccount())
        }
    }
}
