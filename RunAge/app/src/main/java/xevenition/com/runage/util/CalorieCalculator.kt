package xevenition.com.runage.util

object CalorieCalculator {

    /**
     * Calculate the calories burned
     * @param distance in kilometer
     * @param weight in kilograms
     * calculation based on https://fitness.stackexchange.com/a/36045
     */
    fun getCaloriesBurned(distance: Double, weight: Float): Double {
        return distance * weight *  1.036
    }
}