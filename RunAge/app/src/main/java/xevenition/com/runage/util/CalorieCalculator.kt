package xevenition.com.runage.util

object CalorieCalculator {

    /**
     * Calculate the calories burned
     * @param distance in meters
     * @param weight in kilograms
     * calculation based on https://fitness.stackexchange.com/a/36045
     */
    fun getCaloriesBurned(distance: Double, weight: Float): Double {
        return distance * weight *  1.036 / 1000
    }
}