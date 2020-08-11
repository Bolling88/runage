package xevenition.com.runage.util

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.Task
import xevenition.com.runage.R
import xevenition.com.runage.model.RunStats
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.service.GameServicesService
import javax.inject.Inject


class GameServicesUtil @Inject constructor(private val app: Application, private val gameServicesService: GameServicesService){

    fun saveLeaderBoard(leaderboardId: String, score: Long){
        val gameAccount = gameServicesService.getGamesAccount()
        gameAccount?.let {
            Games.getLeaderboardsClient(app, it)
                .submitScore(leaderboardId, score)
        }
    }

    fun unlockAchievement(achievementId: String){
        val gameAccount = gameServicesService.getGamesAccount()
        gameAccount?.let {
            Games.getAchievementsClient(app, it).unlock(achievementId)
        }
    }

    private fun incrementAchievements(achievementId: String, value: Int){
        val gameAccount = gameServicesService.getGamesAccount()
        gameAccount?.let {
            Games.getAchievementsClient(app, it).increment(achievementId, value)
        }
    }

    fun storeAchievementsAndLeaderboards(quest: Quest, runStats: RunStats, userInfo: RunageUser) {
        //Total running distance
        incrementAchievements(app.getString(R.string.achievement_10_k), (runStats.runningDistance.toDouble()/1000).toInt())
        incrementAchievements(app.getString(R.string.achievement_100_k), (runStats.runningDistance.toDouble()/1000).toInt())
        incrementAchievements(app.getString(R.string.achievement_1000_k), (runStats.runningDistance.toDouble()/1000).toInt())
        incrementAchievements(app.getString(R.string.achievement_10_000_k), (runStats.runningDistance.toDouble()/1000).toInt())

        //Number of quests
        if(runStats.runningDistance >= 1000) {
            incrementAchievements(app.getString(R.string.achievement_getting_started_i), 1)
            incrementAchievements(app.getString(R.string.achievement_getting_started_ii), 1)
            incrementAchievements(app.getString(R.string.achievement_getting_started_iii), 1)
        }

        when {
            userInfo.challengeScore.size >= 50 -> {
                unlockAchievement(app.getString(R.string.achievement_completed_challenge_50))
            }
            userInfo.challengeScore.size >= 40 -> {
                unlockAchievement(app.getString(R.string.achievement_completed_challenge_40))
            }
            userInfo.challengeScore.size >= 30 -> {
                unlockAchievement(app.getString(R.string.achievement_completed_challenge_30))
            }
            userInfo.challengeScore.size >= 20 -> {
                unlockAchievement(app.getString(R.string.achievement_completed_challenge_20))
            }
            userInfo.challengeScore.size >= 10 -> {
                unlockAchievement(app.getString(R.string.achievement_completed_challenge_10))
            }
        }

        when {
            userInfo.playerChallengesWon >= 10 -> {
                unlockAchievement(app.getString(R.string.achievement_win_10_player_challenges))
            }
            userInfo.playerChallengesWon >= 5 -> {
                unlockAchievement(app.getString(R.string.achievement_win_5_player_challenges))
            }
            userInfo.playerChallengesWon >= 1 -> {
                unlockAchievement(app.getString(R.string.achievement_win_1_player_challenge))
            }
        }

        //Total calories
        incrementAchievements(app.getString(R.string.achievement_calorie_king_i), quest.calories)
        incrementAchievements(app.getString(R.string.achievement_calorie_king_ii), quest.calories)
        incrementAchievements(app.getString(R.string.achievement_calorie_king_iii), quest.calories)

        //Never stop, running duration in minutes
        incrementAchievements(app.getString(R.string.achievement_never_stop_i),
            (runStats.runningDuration.toDouble()/60).toInt()
        )
        incrementAchievements(app.getString(R.string.achievement_never_stop_ii),
            (runStats.runningDuration.toDouble()/60).toInt()
        )
        incrementAchievements(app.getString(R.string.achievement_never_stop_iii),
            (runStats.runningDuration.toDouble()/60).toInt()
        )

        var totalStars = 0
        for ((key, value) in userInfo.challengeScore) {
            totalStars+= value
        }

        saveLeaderBoard(app.getString(R.string.leaderboard_longest_run_meters), runStats.runningDistance.toLong())
        saveLeaderBoard(app.getString(R.string.leaderboard_most_experience), userInfo.xp.toLong())
        saveLeaderBoard(app.getString(R.string.leaderboard_most_experience_single_run), runStats.xp.toLong())
        saveLeaderBoard(app.getString(R.string.leaderboard_most_stars), totalStars.toLong())
        saveLeaderBoard(app.getString(R.string.leaderboard_total_running_distance_meters), userInfo.distance.toLong())
        saveLeaderBoard(app.getString(R.string.leaderboard_most_challenges_completed), userInfo.challengeScore.size.toLong())
        saveLeaderBoard(app.getString(R.string.leaderboard_total_running_duration), userInfo.duration.toLong().times(1000))
        saveLeaderBoard(app.getString(R.string.leaderboard_most_players_defeated), userInfo.playerChallengesWon.toLong())

        //Long runner
        when {
            runStats.runningDistance >= 30000 -> {
                unlockAchievement(app.getString(R.string.achievement_long_runner_i))
                unlockAchievement(app.getString(R.string.achievement_long_runner_ii))
                unlockAchievement(app.getString(R.string.achievement_long_runner_iii))
            }
            runStats.runningDistance >= 20000 -> {
                unlockAchievement(app.getString(R.string.achievement_long_runner_i))
                unlockAchievement(app.getString(R.string.achievement_long_runner_ii))
            }
            runStats.runningDistance >= 10000 -> {
                unlockAchievement(app.getString(R.string.achievement_long_runner_i))
            }
        }

        //Altitude
        when {
            runStats.altitudeChange >= 300 -> {
                unlockAchievement(app.getString(R.string.achievement_elevationist_iii))
                unlockAchievement(app.getString(R.string.achievement_elevationist_ii))
                unlockAchievement(app.getString(R.string.achievement_elevationist_i))
            }
            runStats.altitudeChange >= 200 -> {
                unlockAchievement(app.getString(R.string.achievement_elevationist_ii))
                unlockAchievement(app.getString(R.string.achievement_elevationist_i))
            }
            runStats.altitudeChange >= 100 -> {
                unlockAchievement(app.getString(R.string.achievement_elevationist_i))
            }
        }

        //If distance was longer then 1000 meters
        if(quest.totalDistance > 1000){
            if(runStats.activityPercentage.getOrDefault(DetectedActivity.IN_VEHICLE, 0.0) > 0.01 ||
                    runStats.activityPercentage.getOrDefault(DetectedActivity.ON_BICYCLE, 0.0) > 0.01){
                //Not eligible
            }else{
                val start = quest.startTimeEpochSeconds
                val end = quest.locations.last().timeStampEpochSeconds
                val totalDuration = end - start
                val secondsPerKm = totalDuration / (quest.totalDistance / 1000)

                saveLeaderBoard(app.getString(R.string.leaderboard_fastest_runner_minkm), secondsPerKm.toLong().times(1000))

                when{
                    secondsPerKm < (60 * 4) ->{
                        unlockAchievement(app.getString(R.string.achievement_fast_runner_iii))
                        unlockAchievement(app.getString(R.string.achievement_fast_runner_ii))
                        unlockAchievement(app.getString(R.string.achievement_fast_runner_i))
                    }
                    secondsPerKm < (60 * 5) ->{
                        unlockAchievement(app.getString(R.string.achievement_fast_runner_ii))
                        unlockAchievement(app.getString(R.string.achievement_fast_runner_i))
                    }
                    secondsPerKm < (60 * 6) ->{
                        unlockAchievement(app.getString(R.string.achievement_fast_runner_i))
                    }
                }
            }
        }
    }

    fun signOut(): Task<Void> {
        val signInClient = GoogleSignIn.getClient(
            app,
            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        )
        return signInClient.signOut()
    }
}