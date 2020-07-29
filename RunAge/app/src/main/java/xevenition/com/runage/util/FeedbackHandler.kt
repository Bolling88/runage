package xevenition.com.runage.util

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.room.entity.Quest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackHandler @Inject constructor(
    saveUtil: SaveUtil,
    private val app: Application,
    private val resourceUtil: ResourceUtil,
    private val textToSpeech: TextToSpeech
) {

    private var isMetric: Boolean = saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
    private var checkPointTimeStamp = 0L

    fun reportCheckpoint(quest: Quest, nextDistanceFeedback: Int, challenge: Challenge?) {

        //if we have not passed any checkpoint, the first checkpoint was at the quest start
        if (checkPointTimeStamp == 0L) {
            checkPointTimeStamp = quest.startTimeEpochSeconds
        }

        val currentTimeMillis = Instant.now().epochSecond
        val duration = currentTimeMillis - quest.startTimeEpochSeconds
        val timeSinceLastCheckpoint = currentTimeMillis - checkPointTimeStamp
        checkPointTimeStamp = currentTimeMillis

        val reportChallengeCompleted = if (challenge != null) {
            if (challenge.distance == nextDistanceFeedback) {
                duration <= challenge.time
            } else {
                false
            }
        } else {
            false
        }

        val reportString = getDistanceFeedback(nextDistanceFeedback, reportChallengeCompleted) + getDurationFeedback(
            resourceUtil.getString(R.string.runage_duration),
            duration
        ) + getCaloriesFeedback(quest) + getDurationFeedback(
            resourceUtil.getString(R.string.runage_pace),
            timeSinceLastCheckpoint
        )

        speak(reportString)
    }

    private fun getDistanceFeedback(nextDistanceFeedback: Int, reportChallengeCompleted: Boolean): String {
        val initialSpeak = if(reportChallengeCompleted){
            resourceUtil.getString(R.string.runage_challenge_completed_info)
        }else{
            resourceUtil.getString(R.string.runage_passed_info)
        }
        return initialSpeak +
                " ${resourceUtil.getString(R.string.runage_distance)} " +
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
        Timber.d("Speaking $string")
        val audioManager =
            app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener {
                    //Handle Focus Change
                }.build()

        audioManager.requestAudioFocus(audioFocusRequest)

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

            override fun onStart(utteranceId: String?) {
                Timber.d("STARTING SPEAKING")
                audioManager.requestAudioFocus(audioFocusRequest)
            }

            override fun onDone(utteranceId: String?) {
                Timber.d("DONE")
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
            }

            override fun onError(utteranceId: String?) {
                Timber.d("ERROR")
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
            }
        })

        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null, string)
    }

    companion object {
        const val METERS_IN_MILE = 1609.344
        const val METERS_IN_KILOMETER = 1000.0
    }
}