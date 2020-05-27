package xevenition.com.runage.model

data class PositionPoint(
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val accuracy: Float,
    val altitude: Double,
    val bearing: Float,
    val timeStampEpochSeconds: Long,
    val activityType: Int
)