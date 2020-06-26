package xevenition.com.runage.model

data class SavedQuest(
    val calories: Int = 0,
    val startTimeEpochSeconds: Long = 0,
    val endTimeEpochSeconds: Long = 0,
    val totalDistance: Int = 0,
    val locations: String = ""
)