package xevenition.com.runage.util

import org.junit.Assert.*
import org.junit.Test

class RunningTimerTest{

    @Test
    fun testConvertMillisToTimerString1(){
        assertEquals("00:00:01", RunningTimer.convertMillisToTimerString(1))
    }

    @Test
    fun testConvertMillisToTimerString2(){
        assertEquals("06:30:53", RunningTimer.convertMillisToTimerString(23453))
    }

    @Test
    fun testConvertMillisToTimerString3(){
        assertEquals("02:46:40", RunningTimer.convertMillisToTimerString(10000))
    }

    @Test
    fun testConvertMillisToTimerString4(){
        assertEquals("27:46:40", RunningTimer.convertMillisToTimerString(100000))
    }

    @Test
    fun testConvertMillisToTimerString5(){
        assertEquals("277:46:40", RunningTimer.convertMillisToTimerString(1000000))
    }

    @Test
    fun testConvertMillisToTimerString6(){
        assertEquals("00:00:00", RunningTimer.convertMillisToTimerString(0))
    }
}