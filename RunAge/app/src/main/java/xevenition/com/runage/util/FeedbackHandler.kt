package xevenition.com.runage.util

import android.app.Application
import android.speech.tts.TextToSpeech
import timber.log.Timber
import javax.inject.Inject

class FeedbackHandler @Inject constructor(saveUtil: SaveUtil, private val textToSpeech: TextToSpeech) {

    private var isMetric: Boolean = saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    private var nextDistanceFeedback = 1

    fun reportDistance(totalDistanceInMeters: Int) {
        if (shouldReport(totalDistanceInMeters)) {

            textToSpeech.speak(
                "You have now passed $nextDistanceFeedback ${if (isMetric) {
                    "kilometers"
                } else {
                    "miles"
                }
                }", TextToSpeech.QUEUE_ADD, null, null
            )

            nextDistanceFeedback++
        }
    }

    fun shouldReport(totalDistanceInMeters: Int): Boolean {
        return totalDistanceInMeters >= getNextDistanceForReport()
    }

    fun getNextDistanceForReport(): Int {
        return if (isMetric) {
            nextDistanceFeedback.times(METERS_IN_KILOMETER)
        } else {
            nextDistanceFeedback.times(METERS_IN_MILE).toInt()
        }
    }

    companion object {
        const val METERS_IN_MILE = 1609.344
        const val METERS_IN_KILOMETER = 1000
    }
}