package xevenition.com.runage.util

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.time.Instant
import java.util.concurrent.TimeUnit

object RunningTimer {

    fun getRunningTimer(startDateEpochSeconds: Long): Observable<String> {
        val currentTimeMillis = Instant.now().epochSecond
        val initialValue = currentTimeMillis - startDateEpochSeconds
        return Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { it + initialValue }
            .map {
                convertTimeToDurationString(it)
            }
            .subscribeOn(Schedulers.computation())
    }

    fun convertTimeToDurationString(seconds: Long): String {
        val times = SeparatorUtil.separateTime(seconds)
        return String.format(
            "%02d:%02d:%02d",
            times.first,
            times.second,
            times.third
        )
    }

    fun getPaceString(durationInSeconds: Long, distanceInMeters: Double): String {
        val secondsPerKm = durationInSeconds.toDouble() / (distanceInMeters/1000)
        val times = SeparatorUtil.separateTime(secondsPerKm.toLong())
        var minutes = times.first * 60 + times.second
        //TODO fix imperial
        return if(minutes > 999) {
            "999:00 min/km"
        }else {
            val seconds = times.third
            "$minutes:$seconds min/km"
        }
    }
}