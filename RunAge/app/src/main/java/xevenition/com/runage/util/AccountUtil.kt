package xevenition.com.runage.util

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Games
import com.google.android.gms.games.Player
import com.google.android.gms.tasks.Task
import javax.inject.Inject

class AccountUtil @Inject constructor(private val app: Application) {
    fun isAccountActive(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(app)
        return if (account != null)
            GoogleSignIn.hasPermissions(account)
        else false
    }

    fun getAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(app)
    }

    fun getPlayerInfo(): Task<Player>? {
        val account = getAccount()
        return if (account != null) {
            Games.getPlayersClient(app, account).currentPlayer
        } else null
    }

    fun openProfileSettings(){
        Games.GamesOptions.GAMES
    }
}