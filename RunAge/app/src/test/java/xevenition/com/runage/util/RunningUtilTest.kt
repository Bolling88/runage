package xevenition.com.runage.util

import android.location.Location
import com.google.android.gms.location.DetectedActivity
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import xevenition.com.runage.model.PositionPoint

class RunningUtilTest{

    @Mock
    lateinit var location: LocationUtil

    @Before
    fun setUp(){
        MockitoAnnotations.initMocks(this)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testCalculateExperience(){
        val positionPoint1 = PositionPoint(59.219560,  17.896985, 0f,     0f, 0.0, 0f, 0, DetectedActivity.RUNNING)
        val positionPoint2 = PositionPoint(59.220926, 17.893164, 0f,     0f, 0.0, 0f, 10, DetectedActivity.RUNNING)
        val positionPoint3 = PositionPoint(59.221909, 17.889623, 0f,     0f, 0.0, 0f, 20, DetectedActivity.RUNNING)
        val positionPoint4 = PositionPoint(59.222074, 17.884720, 0f,     0f, 0.0, 0f, 30, DetectedActivity.WALKING)
        val positionPoint5 = PositionPoint(59.220872, 17.882167, 0f,     0f, 0.0, 0f, 40, DetectedActivity.RUNNING)
        val positionPoint6 = PositionPoint(59.219587, 17.880225, 0f,     0f, 0.0, 0f, 50, DetectedActivity.IN_VEHICLE)
        val positionPoint7 = PositionPoint(59.218050, 17.880386, 0f,     0f, 0.0, 0f, 60, DetectedActivity.RUNNING)
        val positionPoint8 = PositionPoint(59.216469, 17.880611, 0f,     0f, 0.0, 0f, 70, DetectedActivity.IN_VEHICLE)
        val positionPoint9 = PositionPoint(59.214888, 17.880396, 0f,     0f, 0.0, 0f, 80, DetectedActivity.RUNNING)
        val positionPoint10 = PositionPoint(59.213301, 17.880289, 0f,     0f, 0.0, 0f, 90, DetectedActivity.RUNNING)
        val list = listOf(positionPoint1, positionPoint2, positionPoint3, positionPoint4, positionPoint5, positionPoint6, positionPoint7, positionPoint8, positionPoint9, positionPoint10)
        val observer = RunningUtil.calculateExperience(list, location).test()
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValue {
            assertEquals(30, it)
            true
        }
    }
}