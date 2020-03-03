package xevenition.com.runage.util

import android.app.Application
import android.speech.tts.TextToSpeech
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class FeedbackHandlerTest {

    @Mock
    lateinit var saveUtil: SaveUtil
    @Mock
    lateinit var textToSpeech: TextToSpeech

    private lateinit var feedbackHandler: FeedbackHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true))
            .thenReturn(true)
        feedbackHandler = FeedbackHandler(saveUtil, textToSpeech)
    }

    @Test
    fun reportDistance() {
        feedbackHandler.reportDistance(500)
    }

    @Test
    fun testShouldNotReport() {
        val shouldReport = feedbackHandler.shouldReport(500)
        assertFalse(shouldReport)
    }

    @Test
    fun testShouldReport() {
        val shouldReport = feedbackHandler.shouldReport(1900)
        assertTrue(shouldReport)
    }

    @Test
    fun testNextReportDistance(){
        val distance = feedbackHandler.getNextDistanceForReport()
        assertEquals(1000, distance)
    }

    @Test
    fun testNextReportDistanceIncrement(){
        assertEquals(1000, feedbackHandler.getNextDistanceForReport())
        feedbackHandler.reportDistance(1200)
        assertEquals(2000, feedbackHandler.getNextDistanceForReport())
    }
}