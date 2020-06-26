package xevenition.com.runage.util

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Games
import com.google.android.gms.games.Player
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AccountUtil @Inject constructor(private val app: Application) {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isAccountActive(): Boolean {
        return auth.currentUser != null
    }

    fun getGamesAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(app)
    }

    fun getGamesPlayerInfo(): Task<Player>? {
        val account = getGamesAccount()
        return if (account != null) {
            Games.getPlayersClient(app, account).currentPlayer
        } else null
    }

    fun openProfileSettings(){
        Games.GamesOptions.GAMES
    }
}