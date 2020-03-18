package xevenition.com.runage.util

import android.app.Application
import android.speech.tts.TextToSpeech
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.room.entity.Quest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackHandler @Inject constructor(
    saveUtil: SaveUtil,
    private val resourceUtil: ResourceUtil,
    private val textToSpeech: TextToSpeech
) {

    private var isMetric: Boolean = saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    private var nextDistanceFeedback = 1

    fun reportCheckpoint(quest: Quest) {
        val totalDistanceInMeters = quest.totalDistance
        if (shouldReport(totalDistanceInMeters)) {

            val reportString =
                "${resourceUtil.getString(R.string.runage_passed_info)} " +
                        "$nextDistanceFeedback " +
                        if (isMetric) {
                            resourceUtil.getString(R.string.kilometer)
                        } else {
                            resourceUtil.getString(R.string.miles)
                        } +
                        ". ${resourceUtil.getString(R.string.runage_calories_burned)} " +
                        "${quest.calories.toInt()}. "

            speak(reportString)

            nextDistanceFeedback++
        }
    }

    fun separateValue(distanceInMeters: Double): Triple<Int, Int, Int> {
        val thousands = distanceInMeters - (distanceInMeters % 1000)
        val hundreds = (distanceInMeters - thousands) % 1000 - (distanceInMeters - thousands) % 100
        val remaining = (distanceInMeters - thousands) % 100
        return Triple(thousands.toInt(), hundreds.toInt(), remaining.toInt())
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

    fun speak(string: String) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    companion object {
        const val METERS_IN_MILE = 1609.344
        const val METERS_IN_KILOMETER = 1000.0
    }
}