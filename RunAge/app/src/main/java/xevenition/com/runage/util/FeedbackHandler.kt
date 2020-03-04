package xevenition.com.runage.util

import android.app.Application
import android.speech.tts.TextToSpeech
import timber.log.Timber
import xevenition.com.runage.R
import javax.inject.Inject

class FeedbackHandler @Inject constructor(saveUtil: SaveUtil, private val resourceUtil: ResourceUtil, private val textToSpeech: TextToSpeech) {

    private var isMetric: Boolean = saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    private var nextDistanceFeedback = 1

    fun reportDistance(totalDistanceInMeters: Double) {
        if (shouldReport(totalDistanceInMeters)) {
            textToSpeech.speak(
                "${resourceUtil.getString(R.string.runage_passed_info)} $nextDistanceFeedback ${if (isMetric) {
                    resourceUtil.getString(R.string.kilometer)
                } else {
                    resourceUtil.getString(R.string.miles)
                }
                }", TextToSpeech.QUEUE_FLUSH, null, null
            )

            nextDistanceFeedback++
        }
    }

    fun shouldReport(totalDistanceInMeters: Double): Boolean {
        return totalDistanceInMeters >= getNextDistanceForReport()
    }

    fun getNextDistanceForReport(): Double {
        return if (isMetric) {
            nextDistanceFeedback.times(METERS_IN_KILOMETER)
        } else {
            nextDistanceFeedback.times(METERS_IN_MILE)
        }
    }

    fun giveInitialFeedback() {
        textToSpeech.speak("Fuck yeah lets go mother fucker", TextToSpeech.QUEUE_ADD, null,null)
    }

    companion object {
        const val METERS_IN_MILE = 1609.344
        const val METERS_IN_KILOMETER = 1000.0
    }
}