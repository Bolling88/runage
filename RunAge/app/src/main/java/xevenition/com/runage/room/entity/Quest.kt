package xevenition.com.runage.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xevenition.com.runage.model.PositionPoint

@Entity(tableName = "quest")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "start_time_epoch_seconds") val startTimeEpochSeconds: Long,
    @ColumnInfo(name = "speed") val speed: Float,
    @ColumnInfo(name = "locations") var locations: MutableList<PositionPoint>,
    @ColumnInfo(name = "total_distance") var totalDistance: Double,
    @ColumnInfo(name = "calories") var calories: Double
) {

    @Ignore
    constructor(startTimeEpochSeconds: Long) : this(
        id = 0,
        startTimeEpochSeconds = startTimeEpochSeconds,
        speed = 0f,
        locations = mutableListOf<PositionPoint>(),
        totalDistance = 0.0,
        calories = 0.0
    )
}