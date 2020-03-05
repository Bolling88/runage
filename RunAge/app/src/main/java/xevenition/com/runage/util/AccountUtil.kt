package xevenition.com.runage.util

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import javax.inject.Inject

class AccountUtil @Inject constructor(private val app: Application) {
    fun isAccountActive(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(app)
        return if (account != null)
            GoogleSignIn.hasPermissions(account)
        else false
    }
}