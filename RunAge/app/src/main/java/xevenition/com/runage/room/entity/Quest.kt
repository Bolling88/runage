package xevenition.com.runage.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xevenition.com.runage.model.PositionPoint

@Entity(tableName = "quest")
data class Quest(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "start_time_millis") val startTimeMillis: Long,
        @ColumnInfo(name = "speed") val speed: Float,
        @ColumnInfo(name = "locations") var locations: MutableList<PositionPoint>
) {

    @Ignore
    constructor(startTimeMillis: Long) : this(
            id = 0,
            startTimeMillis = startTimeMillis,
            speed = 0f,
            locations = mutableListOf<PositionPoint>()
    )
}