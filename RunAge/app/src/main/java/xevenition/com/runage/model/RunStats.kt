package xevenition.com.runage.model

data class RunStats(
    val xp: Int,
    val runningDistance: Int,
    val runningDuration: Int,
    val altitudeChange: Int,
    val activityPercentage: Map<Int, Double>
)