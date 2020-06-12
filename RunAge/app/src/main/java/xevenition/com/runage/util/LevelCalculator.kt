package xevenition.com.runage.util

import kotlin.math.pow
import kotlin.math.sqrt

object LevelCalculator {

    private const val LEVEL_CONSTANT = 0.1

    fun getLevel(experience: Int): Int {
         return (LEVEL_CONSTANT * sqrt(experience.toDouble())).toInt()
    }

    fun getXpForLevel(level: Int): Int {
        return (level.toDouble().div(LEVEL_CONSTANT)).pow(2).toInt()
    }
}