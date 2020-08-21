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

    fun getXpCalculation(durationInSeconds: Double, distanceInMeters: Double): Int {
        val km = distanceInMeters/1000
        val secondsPerKm = durationInSeconds / km
        var paceModifiers = 1500 - secondsPerKm
        if(paceModifiers < 0)
            paceModifiers = 0.0
        return paceModifiers.times(km).div(12).toInt()
    }
}