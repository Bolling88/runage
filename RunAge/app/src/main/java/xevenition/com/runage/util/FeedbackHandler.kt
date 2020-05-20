package xevenition.com.runage.util

import android.speech.tts.TextToSpeech
import xevenition.com.runage.R
import xevenition.com.runage.room.entity.Quest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackHandler @Inject constructor(
    saveUtil: SaveUtil,
    private val resourceUtil: ResourceUtil,
    private val textToSpeech: TextToSpeech
) {

    private var isMetric: Boolean = saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    private var checkPointTimeStamp = 0L

    fun reportCheckpoint(quest: Quest, nextDistanceFeedback: Int) {

        //if we have not passed any checkpoint, the first checkpoint was at the quest start
        if (checkPointTimeStamp == 0L) {
            checkPointTimeStamp = quest.startTimeEpochSeconds
        }

        val currentTimeMillis = Instant.now().epochSecond
        val initialValue = currentTimeMillis - quest.startTimeEpochSeconds
        val timeSinceLastCheckpoint = currentTimeMillis - checkPointTimeStamp
        checkPointTimeStamp = currentTimeMillis

        val reportString = getDistanceFeedback(nextDistanceFeedback) + getDurationFeedback(
            resourceUtil.getString(R.string.runage_duration),
            initialValue
        ) + getCaloriesFeedback(quest) + getDurationFeedback(
            resourceUtil.getString(R.string.runage_pace),
            timeSinceLastCheckpoint
        )

        speak(reportString)
    }

    private fun getDistanceFeedback(nextDistanceFeedback: Int): String {
        return "${resourceUtil.getString(R.string.runage_passed_info)} " +
                "$nextDistanceFeedback " +
                if (isMetric) {
                    if (nextDistanceFeedback == 1) {
                        resourceUtil.getString(R.string.kilometer)
                    } else {
                        resourceUtil.getString(R.string.kilometers)
                    }
                } else {
                    if (nextDistanceFeedback == 1) {
                        resourceUtil.getString(R.string.mile)
                    } else {
                        resourceUtil.getString(R.string.miles)
                    }
                }
    }

    private fun getDurationFeedback(feedback: String, durationInSeconds: Long): String {
        val times = SeparatorUtil.separateTime(durationInSeconds)
        val part1 = ". $feedback. "
        val part2 = when {
            times.first == 1L -> "${times.first} ${resourceUtil.getString(R.string.runage_hour)}. "
            times.first > 1L -> "${times.first} ${resourceUtil.getString(R.string.runage_hours)}. "
            else -> ""
        }
        val part3 = when {
            times.second == 1L -> "${times.second} ${resourceUtil.getString(R.string.runage_minute)}. "
            times.second > 1L -> "${times.second} ${resourceUtil.getString(R.string.runage_minutes)}. "
            else -> ""
        }
        val part4 = when {
            times.third == 1L -> "${times.third} ${resourceUtil.getString(R.string.runage_second)}. "
            times.third > 1L -> "${times.third} ${resourceUtil.getString(R.string.runage_seconds)}. "
            else -> ""
        }
        return part1 + part2 + part3 + part4
    }

    private fun getCaloriesFeedback(quest: Quest): String {
        val caloriesValues = SeparatorUtil.separateValue(quest.calories.toInt())
        return ". ${resourceUtil.getString(R.string.runage_calories_burned)} " +
                if (caloriesValues.first != 0) {
                    "${caloriesValues.first}. "
                } else {
                    ""
                } +
                if (caloriesValues.second != 0) {
                    "${caloriesValues.second}.  "
                } else {
                    ""
                } +
                if (caloriesValues.third != 0) {
                    "${caloriesValues.third}. "
                } else {
                    ""
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