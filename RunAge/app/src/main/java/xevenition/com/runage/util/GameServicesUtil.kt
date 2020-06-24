package xevenition.com.runage.util

import android.app.Application
import com.google.android.gms.games.Games
import javax.inject.Inject

class GameServicesUtil @Inject constructor(private val app: Application, private val accountUtil: AccountUtil){

    fun saveLeaderBoard(leaderboardId: String, score: Long){
        val gameAccount = accountUtil.getGamesAccount()
        gameAccount?.let {
            Games.getLeaderboardsClient(app, it)
                .submitScore(leaderboardId, score)
        }
    }
}