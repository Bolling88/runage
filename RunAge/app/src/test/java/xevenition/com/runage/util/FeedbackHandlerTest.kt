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
        feedbackHandler.reportCheckpoint(quest, 1)
    }
}