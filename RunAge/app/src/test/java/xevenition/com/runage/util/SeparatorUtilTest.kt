package xevenition.com.runage.util

import org.junit.Assert.*
import org.junit.Test

class SeparatorUtilTest{

    @Test
    fun testValueSeparator(){
        val values = SeparatorUtil.separateValue(13455)
        assertEquals(13000, values.first)
        assertEquals(400, values.second)
        assertEquals(55, values.third)
    }

    @Test
    fun testTimeSeparator1(){
        val times = SeparatorUtil.separateTime(24567)
        assertEquals(6, times.first)
        assertEquals(49, times.second)
        assertEquals(27, times.third)
    }

    @Test
    fun testTimeSeparator2(){
        val times = SeparatorUtil.separateTime(0)
        assertEquals(0, times.first)
        assertEquals(0, times.second)
        assertEquals(0, times.third)
    }

    @Test
    fun testTimeSeparator3(){
        val times = SeparatorUtil.separateTime(3600)
        assertEquals(1, times.first)
        assertEquals(0, times.second)
        assertEquals(0, times.third)
    }
}