package xevenition.com.runage.util

import org.junit.Assert.*
import org.junit.Test

class CalorieCalculatorTest{

    @Test
    fun testCaloriesCalculations1(){
        val calories = CalorieCalculator.getCaloriesBurned(10000.0, 75.0)
        assertEquals(777, calories)
    }
}