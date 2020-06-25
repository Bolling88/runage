package xevenition.com.runage.util

import android.app.Application
import com.google.android.gms.games.Games
import com.google.android.gms.location.DetectedActivity
import xevenition.com.runage.R
import xevenition.com.runage.model.RunStats
import xevenition.com.runage.room.entity.Quest
import javax.inject.Inject

class GameServicesUtil @Inject constructor(private val app: Application, private val accountUtil: AccountUtil){

    fun saveLeaderBoard(leaderboardId: String, score: Long){
        val gameAccount = accountUtil.getGamesAccount()
        gameAccount?.let {
            Games.getLeaderboardsClient(app, it)
                .submitScore(leaderboardId, score)
        }
    }

    fun unlockAchievement(achievementId: String){
        val gameAccount = accountUtil.getGamesAccount()
        gameAccount?.let {
            Games.getAchievementsClient(app, it).unlock(achievementId)
        }
    }

    private fun incrementAchievements(achievementId: String, value: Int){
        val gameAccount = accountUtil.getGamesAccount()
        gameAccount?.let {
            Games.getAchievementsClient(app, it).increment(achievementId, value)
        }
    }

    fun storeAchievementsAndLeaderboards(quest: Quest, runStats: RunStats) {
        incrementAchievements(app.getString(R.string.achievement_10_k), runStats.runningDistance)
        incrementAchievements(app.getString(R.string.achievement_100_k), runStats.runningDistance)
        incrementAchievements(app.getString(R.string.achievement_1000_k), runStats.runningDistance)
        incrementAchievements(app.getString(R.string.achievement_10_000_k), runStats.runningDistance)
        incrementAchievements(app.getString(R.string.achievement_calorie_king_i), quest.calories)
        incrementAchievements(app.getString(R.string.achievement_calorie_king_ii), quest.calories)
        incrementAchievements(app.getString(R.string.achievement_never_stop_i), runStats.runningDuration)
        incrementAchievements(app.getString(R.string.achievement_never_stop_ii), runStats.runningDuration)
        incrementAchievements(app.getString(R.string.achievement_never_stop_iii), runStats.runningDuration)

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

        if(quest.totalDistance > 1000){
            if(runStats.activityPercentage.containsKey(DetectedActivity.IN_VEHICLE) ||
                    runStats.activityPercentage.containsKey(DetectedActivity.ON_BICYCLE)){
                //Not eligible
            }else{
                val start = quest.startTimeEpochSeconds
                val end = quest.locations.last().timeStampEpochSeconds
                val totalDuration = end - start
                val secondsPerKm = totalDuration / (quest.totalDistance / 1000)
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
}