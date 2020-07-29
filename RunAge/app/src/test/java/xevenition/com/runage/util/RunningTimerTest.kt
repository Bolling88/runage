package xevenition.com.runage.util

import com.google.android.gms.location.DetectedActivity
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import xevenition.com.runage.R
import xevenition.com.runage.model.PositionPoint

class RunningTimerTest{

    @Mock
    lateinit var locationUtil: LocationUtil
    @Mock
    lateinit var location: LocationUtil
    @Mock
    lateinit var resourceUtil: ResourceUtil
    @Mock
    lateinit var saveUtil: SaveUtil


    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T
    lateinit var runningUtil: RunningUtil

    @Before
    fun setUp(){
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(location.getDistanceBetweenPositionPoints(any(), any()))
            .thenAnswer {
                20f
            }
        Mockito.`when`(resourceUtil.getString(R.string.runage_min_km))
            .thenAnswer {
                "min/km"
            }
        Mockito.`when`(resourceUtil.getString(R.string.runage_min_mi))
            .thenAnswer {
                "min/mi"
            }
        Mockito.`when`(saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC))
            .thenAnswer {
                true
            }
        runningUtil = RunningUtil(resourceUtil, saveUtil)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testConvertMillisToTimerString1(){
        assertEquals("00:00:01", runningUtil.convertTimeToDurationString(1))
    }

    @Test
    fun testConvertMillisToTimerString2(){
        assertEquals("06:30:53", runningUtil.convertTimeToDurationString(23453))
    }

    @Test
    fun testConvertMillisToTimerString3(){
        assertEquals("02:46:40", runningUtil.convertTimeToDurationString(10000))
    }

    @Test
    fun testConvertMillisToTimerString4(){
        assertEquals("27:46:40", runningUtil.convertTimeToDurationString(100000))
    }

    @Test
    fun testConvertMillisToTimerString5(){
        assertEquals("277:46:40", runningUtil.convertTimeToDurationString(1000000))
    }

    @Test
    fun testConvertMillisToTimerString6(){
        assertEquals("00:00:00", runningUtil.convertTimeToDurationString(0))
    }

    @Test
    fun testPaceString1(){
        assertEquals("0:0 min/mi", runningUtil.getPaceString(0, 0.0))
    }

    @Test
    fun testPaceString2(){
        assertEquals("1:36 min/mi", runningUtil.getPaceString(60, 1000.0))
    }

    @Test
    fun testPaceString3(){
        assertEquals("3:45 min/mi", runningUtil.getPaceString(140, 1000.0))
    }

    @Test
    fun testPaceString4(){
        assertEquals("999:00 min/mi", runningUtil.getPaceString(99999999999999, 1000.0))
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
        val observer = runningUtil.processRunningStats(list, locationUtil).test()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue {
            assertEquals(0.3, it.activityPercentage[DetectedActivity.RUNNING])
            assertEquals(0.5, it.activityPercentage[DetectedActivity.IN_VEHICLE])
            assertEquals(0.2, it.activityPercentage[DetectedActivity.WALKING])
            true
        }
    }

    @Test
    fun testCalculateExperience() {
        val positionPoint1 =
            PositionPoint(59.219560, 17.896985, 0f, 0f, 0.0, 0f, 0, DetectedActivity.RUNNING)
        val positionPoint2 =
            PositionPoint(59.220926, 17.893164, 0f, 0f, 0.0, 0f, 10, DetectedActivity.RUNNING)
        val positionPoint3 =
            PositionPoint(59.221909, 17.889623, 0f, 0f, 0.0, 0f, 20, DetectedActivity.RUNNING)
        val positionPoint4 =
            PositionPoint(59.222074, 17.884720, 0f, 0f, 0.0, 0f, 30, DetectedActivity.WALKING)
        val positionPoint5 =
            PositionPoint(59.220872, 17.882167, 0f, 0f, 0.0, 0f, 40, DetectedActivity.RUNNING)
        val positionPoint6 =
            PositionPoint(59.219587, 17.880225, 0f, 0f, 0.0, 0f, 50, DetectedActivity.IN_VEHICLE)
        val positionPoint7 =
            PositionPoint(59.218050, 17.880386, 0f, 0f, 0.0, 0f, 60, DetectedActivity.RUNNING)
        val positionPoint8 =
            PositionPoint(59.216469, 17.880611, 0f, 0f, 0.0, 0f, 70, DetectedActivity.IN_VEHICLE)
        val positionPoint9 =
            PositionPoint(59.214888, 17.880396, 0f, 0f, 0.0, 0f, 80, DetectedActivity.RUNNING)
        val positionPoint10 =
            PositionPoint(59.213301, 17.880289, 0f, 0f, 0.0, 0f, 90, DetectedActivity.RUNNING)
        val list = listOf(
            positionPoint1,
            positionPoint2,
            positionPoint3,
            positionPoint4,
            positionPoint5,
            positionPoint6,
            positionPoint7,
            positionPoint8,
            positionPoint9,
            positionPoint10
        )
        val observer = runningUtil.processRunningStats(list, location).test()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue {
            assertEquals(5, it.xp)
            assertEquals(120, it.runningDistance)
            assertEquals(60, it.runningDuration)
            true
        }
    }

    @Test
    fun testSecondsBehind1(){
        val secondsBehind = runningUtil.getSecondsBehindCheckpoint(480, 1500.0, 2000)
        assertEquals(160, secondsBehind)
    }
}
