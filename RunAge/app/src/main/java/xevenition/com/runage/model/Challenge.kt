package xevenition.com.runage.model

data class Challenge (
    val level : Int,
    val distance : Int,
    val time : Int,
    val experience : Int,
    val gold : Int,
    val isBoss : Boolean,
    val requiredLevel : Int
)