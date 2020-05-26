package xevenition.com.runage.util

import dalvik.annotation.TestTarget
import org.junit.Assert.*
import org.junit.Test

class RunningTimerTest{

    @Test
    fun testConvertMillisToTimerString1(){
        assertEquals("00:00:01", RunningTimer.convertTimeToDurationString(1))
    }

    @Test
    fun testConvertMillisToTimerString2(){
        assertEquals("06:30:53", RunningTimer.convertTimeToDurationString(23453))
    }

    @Test
    fun testConvertMillisToTimerString3(){
        assertEquals("02:46:40", RunningTimer.convertTimeToDurationString(10000))
    }

    @Test
    fun testConvertMillisToTimerString4(){
        assertEquals("27:46:40", RunningTimer.convertTimeToDurationString(100000))
    }

    @Test
    fun testConvertMillisToTimerString5(){
        assertEquals("277:46:40", RunningTimer.convertTimeToDurationString(1000000))
    }

    @Test
    fun testConvertMillisToTimerString6(){
        assertEquals("00:00:00", RunningTimer.convertTimeToDurationString(0))
    }

    @Test
    fun testPaceString1(){
        assertEquals("0:0 min/km", RunningTimer.getPaceString(0, 0.0))
    }

    @Test
    fun testPaceString2(){
        assertEquals("1:0 min/km", RunningTimer.getPaceString(60, 1000.0))
    }

    @Test
    fun testPaceString3(){
        assertEquals("2:20 min/km", RunningTimer.getPaceString(140, 1000.0))
    }

    @Test
    fun testPaceString4(){
        assertEquals("999:00 min/km", RunningTimer.getPaceString(99999999999999, 1000.0))
    }
}