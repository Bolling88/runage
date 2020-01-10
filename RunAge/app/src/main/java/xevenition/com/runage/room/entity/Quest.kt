package xevenition.com.runage.room.entity

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "quest")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "start_time_millis") val startTimeMillis: Long,
    @ColumnInfo(name = "locations") var locations: List<Location>
){

    @Ignore
    constructor(startTimeMillis: Long): this(
        id = 0,
        startTimeMillis = startTimeMillis,
        locations = emptyList()
    )
}