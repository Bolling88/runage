package xevenition.com.runage.fragment.login

import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PlayGamesAuthProvider
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.util.SingleLiveEvent
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.SaveUtil

class LoginViewModel(
    private val saveUtil: SaveUtil,
    private val resourceUtil: ResourceUtil,
    private val fireStoreService: FireStoreService
) : BaseViewModel() {

    private var permissionsGranted = false

    val observableLoginClicked = SingleLiveEvent<Unit>()

    private fun onLoginSuccess() {
        fireStoreService.storeUserIfNotExists()
        if (!saveUtil.getBoolean(SaveUtil.KEY_INITIAL_SETTINGS_COMPLETED)) {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToSettingsFragment())
        } else if (!permissionsGranted) {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToPermissionFragment())
        } else {
            observableNavigateTo.postValue(LoginFragmentDirections.actionLoginFragmentToMainFragment())
        }
    }

    // Call this both in the silent sign-in task's OnCompleteListener and in the
    // Activity's onActivityResult handler.
    fun firebaseAuthWithPlayGames(acct: GoogleSignInAccount) {
        val auth = FirebaseAuth.getInstance()
        val credential = PlayGamesAuthProvider.getCredential(acct.serverAuthCode!!)
        auth.signInWithCredential(credential)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                  onLoginSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w("signInWithCredential:failure ${task.exception}")
                    observableToast.postValue(resourceUtil.getString(R.string.runage_login_failed))
                    observableBackNavigation.call()
                }
            }
    }

    fun onLoginClicked() {
        observableLoginClicked.call()
    }

    fun permissionsGranted(granted: Boolean) {
        permissionsGranted = granted
    }
}
