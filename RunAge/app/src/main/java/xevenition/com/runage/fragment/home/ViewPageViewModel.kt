package xevenition.com.runage.fragment.home

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.util.AccountUtil

class ViewPageViewModel(private  val questRepository: QuestRepository,
                        private val accountUtil: AccountUtil
) : BaseViewModel() {

    private val _observableUserAccount = MutableLiveData<GoogleSignInAccount>()
    val observableUserAccount: LiveData<GoogleSignInAccount> = _observableUserAccount

    @SuppressLint("CheckResult")
    fun onQuestFinished(questId: Int) {
        questRepository.getSingleQuest(questId)
            .subscribe({
                //Quest exists, show summary
                observableNavigateTo.postValue(ViewPageFragmentDirections.actionViewPageFragmentToSummaryFragment(questId))
            },{
                //Quest didn't even start, do nothing
            })
    }

    fun onResume() {
        if(!accountUtil.isAccountActive() || accountUtil.getGamesAccount() == null){
            observableBackNavigation.call()
        }else{
            _observableUserAccount.postValue(accountUtil.getGamesAccount())
        }
    }
}
