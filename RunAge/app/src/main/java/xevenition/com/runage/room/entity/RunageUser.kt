package xevenition.com.runage.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class RunageUser(
    @PrimaryKey val userId: String = "",
    @ColumnInfo(name = "xp") val xp: Int = 0,
    @ColumnInfo(name = "calories") val calories: Int = 0,
    @ColumnInfo(name = "distance") val distance: Int = 0,
    @ColumnInfo(name = "following") val following: List<String> = listOf(),
    @ColumnInfo(name = "followers") val followers: List<String> = listOf(),
    @ColumnInfo(name = "challengeScore") val challengeScore: Map<String, Int> = mapOf(),
    @ColumnInfo(name = "duration") val duration: Int = 0,
    @ColumnInfo(name = "gameServicesId") val gameServicesId: String = "",
    @ColumnInfo(name = "playerName") val playerName: String = "",
    @ColumnInfo(name = "completedRuns") val completedRuns: Int = 0,
    @ColumnInfo(name = "playerChallengesWon") val playerChallengesWon: Int = 0,
    @ColumnInfo(name = "playerChallengesLost") val playerChallengesLost: Int = 0
)