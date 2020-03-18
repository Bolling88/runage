package xevenition.com.runage.util

import android.speech.tts.TextToSpeech
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import xevenition.com.runage.room.entity.Quest

class FeedbackHandlerTest {

    @Mock
    lateinit var saveUtil: SaveUtil
    @Mock
    lateinit var resourceUtil: ResourceUtil
    @Mock
    lateinit var textToSpeech: TextToSpeech

    private lateinit var feedbackHandler: FeedbackHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true))
            .thenReturn(true)
        feedbackHandler = FeedbackHandler(saveUtil, resourceUtil, textToSpeech)
    }

    @Test
    fun reportDistance() {
        val quest = Quest(0)
        quest.totalDistance = 500.0
        feedbackHandler.reportCheckpoint(quest)
    }

    @Test
    fun testShouldNotReport() {
        val shouldReport = feedbackHandler.shouldReport(500.0)
        assertFalse(shouldReport)
    }

    @Test
    fun testShouldReport() {
        val shouldReport = feedbackHandler.shouldReport(1900.0)
        assertTrue(shouldReport)
    }

    @Test
    fun testNextReportDistance(){
        val distance = feedbackHandler.getNextDistanceForReport()
        assertEquals(FeedbackHandler.METERS_IN_KILOMETER, distance, 0.0)
    }

    @Test
    fun testNextReportDistanceIncrement(){
        assertEquals(FeedbackHandler.METERS_IN_KILOMETER, feedbackHandler.getNextDistanceForReport(), 0.0)
        val quest = Quest(0)
        quest.totalDistance = 1200.0
        feedbackHandler.reportCheckpoint(quest)
        assertEquals(FeedbackHandler.METERS_IN_KILOMETER*2, feedbackHandler.getNextDistanceForReport(), 0.0)
    }

    @Test
    fun testValueSeparator(){
        val values = feedbackHandler.separateValue(13455.0)
        assertEquals(13000, values.first)
        assertEquals(400, values.second)
        assertEquals(55, values.third)
    }
}