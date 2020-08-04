package xevenition.com.runage.model

data class UserInfo(
    val userId: String = "",
    val xp: Int = 0,
    val calories: Int = 0,
    val distance: Int = 0,
    val following: List<String> = listOf(),
    val challengeScore: Map<String, Int> = mapOf(),
    val duration: Int = 0
)