package xevenition.com.runage.util

import com.google.android.gms.location.DetectedActivity
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import xevenition.com.runage.model.PositionPoint

class RunningTimerTest{

    @Before
    fun setUp(){
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testConvertMillisToTimerString1(){
        assertEquals("00:00:01", RunningUtil.convertTimeToDurationString(1))
    }

    @Test
    fun testConvertMillisToTimerString2(){
        assertEquals("06:30:53", RunningUtil.convertTimeToDurationString(23453))
    }

    @Test
    fun testConvertMillisToTimerString3(){
        assertEquals("02:46:40", RunningUtil.convertTimeToDurationString(10000))
    }

    @Test
    fun testConvertMillisToTimerString4(){
        assertEquals("27:46:40", RunningUtil.convertTimeToDurationString(100000))
    }

    @Test
    fun testConvertMillisToTimerString5(){
        assertEquals("277:46:40", RunningUtil.convertTimeToDurationString(1000000))
    }

    @Test
    fun testConvertMillisToTimerString6(){
        assertEquals("00:00:00", RunningUtil.convertTimeToDurationString(0))
    }

    @Test
    fun testPaceString1(){
        assertEquals("0:0 min/km", RunningUtil.getPaceString(0, 0.0))
    }

    @Test
    fun testPaceString2(){
        assertEquals("1:0 min/km", RunningUtil.getPaceString(60, 1000.0))
    }

    @Test
    fun testPaceString3(){
        assertEquals("2:20 min/km", RunningUtil.getPaceString(140, 1000.0))
    }

    @Test
    fun testPaceString4(){
        assertEquals("999:00 min/km", RunningUtil.getPaceString(99999999999999, 1000.0))
    }

    @Test
    fun testPercentageCalculator(){
        val positionPoint1 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.RUNNING)
        val positionPoint2 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.RUNNING)
        val positionPoint3 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.RUNNING)
        val positionPoint4 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.WALKING)
        val positionPoint5 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.WALKING)
        val positionPoint6 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.IN_VEHICLE)
        val positionPoint7 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.IN_VEHICLE)
        val positionPoint8 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.IN_VEHICLE)
        val positionPoint9 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.IN_VEHICLE)
        val positionPoint10 = PositionPoint(0.0, 0.0, 0f,     0f, 0.0, 0f, 0, DetectedActivity.IN_VEHICLE)
        val list = listOf(positionPoint1, positionPoint2, positionPoint3, positionPoint4, positionPoint5, positionPoint6, positionPoint7, positionPoint8, positionPoint9, positionPoint10)
        val observer = RunningUtil.calculateActivityDurationPercentage(list).test()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue {
            assertEquals(0.3, it[DetectedActivity.RUNNING])
            assertEquals(0.5, it[DetectedActivity.IN_VEHICLE])
            assertEquals(0.2, it[DetectedActivity.WALKING])
            true
        }
    }

}