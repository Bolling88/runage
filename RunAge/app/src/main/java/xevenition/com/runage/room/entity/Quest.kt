package xevenition.com.runage.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.model.PositionPoint

@Entity(tableName = "quest")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "start_time_epoch_seconds") val startTimeEpochSeconds: Long,
    @ColumnInfo(name = "locations") var locations: MutableList<PositionPoint>,
    @ColumnInfo(name = "total_distance") var totalDistance: Double,
    @ColumnInfo(name = "calories") var calories: Int,
    @ColumnInfo(name = "level") var level: Int,
    @ColumnInfo(name = "levelDistance") var levelDistance: Int,
    @ColumnInfo(name = "levelTime") var levelTime: Int,
    @ColumnInfo(name = "isPlayerChallenge") var isPlayerChallenge: Boolean,
    @ColumnInfo(name = "levelExperience") var levelExperience: Int,
    @ColumnInfo(name = "levelStars") var levelStars: Int
) {

    @Ignore
    constructor(startTimeEpochSeconds: Long) : this(
        id = 0,
        startTimeEpochSeconds = startTimeEpochSeconds,
        locations = mutableListOf<PositionPoint>(),
        totalDistance = 0.0,
        calories = 0,
        level = -1,
        levelDistance = 0,
        levelTime = 0,
        isPlayerChallenge = false,
        levelExperience = 0,
        levelStars = 0
    )

    @Ignore
    constructor(startTimeEpochSeconds: Long, challenge: Challenge) : this(
        id = 0,
        startTimeEpochSeconds = startTimeEpochSeconds,
        locations = mutableListOf<PositionPoint>(),
        totalDistance = 0.0,
        calories = 0,
        level = challenge.level,
        levelDistance = challenge.distance,
        levelTime = challenge.time,
        isPlayerChallenge = challenge.isPlayerChallenge,
        levelExperience = challenge.experience,
        levelStars = 0
    )
}