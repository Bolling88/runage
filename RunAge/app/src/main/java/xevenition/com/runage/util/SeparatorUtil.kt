package xevenition.com.runage.util

import java.util.concurrent.TimeUnit

object SeparatorUtil {
    fun separateTime(durationInSeconds: Long): Triple<Long, Long, Long> {
        val hours = TimeUnit.SECONDS.toHours(durationInSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(durationInSeconds))
        val seconds = TimeUnit.SECONDS.toSeconds(durationInSeconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(durationInSeconds))
        return Triple(hours, minutes, seconds)
    }

    fun separateValue(value: Int): Triple<Int, Int, Int> {
        val thousands = value - (value % 1000)
        val hundreds = (value - thousands) % 1000 - (value - thousands) % 100
        val remaining = (value - thousands) % 100
        return Triple(thousands, hundreds, remaining)
    }
}