package xevenition.com.runage.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LevelCalculatorTest{

    @Test
    fun testLevelCalculations1(){
        assertEquals(1, LevelCalculator.getLevel(100));
    }

    @Test
    fun testLevelCalculations2(){
        assertEquals(2, LevelCalculator.getLevel(400));
    }

    @Test
    fun testLevelCalculations3(){
        assertEquals(3, LevelCalculator.getLevel(1300));
    }

    @Test
    fun testLevelCalculations4(){
        assertEquals(4, LevelCalculator.getLevel(2000));
    }

    @Test
    fun testLevelCalculations5(){
        assertEquals(5, LevelCalculator.getLevel(3000));
    }

    @Test
    fun testLevelCalculations6(){
        assertEquals(6, LevelCalculator.getLevel(4000));
    }

    @Test
    fun testLevelCalculations7(){
        assertEquals(7, LevelCalculator.getLevel(5000));
    }

    @Test
    fun testLevelCalculations8(){
        assertEquals(8, LevelCalculator.getLevel(7000));
    }

    @Test
    fun testXpCalculation1(){
        assertEquals(100, LevelCalculator.getXpForLevel(1));
    }

    @Test
    fun testXpCalculation2(){
        assertEquals(400, LevelCalculator.getXpForLevel(2));
    }

    @Test
    fun testXpCalculation3(){
        assertEquals(900, LevelCalculator.getXpForLevel(3));
    }

    @Test
    fun testXpCalculation4(){
        assertEquals(1600, LevelCalculator.getXpForLevel(4));
    }

    @Test
    fun testXpCalculation5(){
        assertEquals(2500, LevelCalculator.getXpForLevel(5));
    }

    @Test
    fun testXpCalculation6(){
        assertEquals(3600, LevelCalculator.getXpForLevel(6));
    }
}