package xevenition.com.runage.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SavedQuest(
    val questId: String = "",
    val totalDistance: Int = 0,
    val calories: Int = 0,
    val playerName: String = "",
    val playerImageUri: String = "",
    val userId: String = "",
    val xp: Int = 0,
    val startTimeEpochSeconds: Long = 0,
    val endTimeEpochSeconds: Long = 0,
    val runningPercentage: Double = 0.0,
    val walkingPercentage: Double = 0.0,
    val bicyclingPercentage: Double = 0.0,
    val stillPercentage: Double = 0.0,
    val drivingPercentage: Double = 0.0,
    val altitude: Int = 0,
    val locations: String = ""
) : Parcelable