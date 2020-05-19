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
                convertMillisToTimerString(it)
            }
            .subscribeOn(Schedulers.computation())
    }

    fun convertMillisToTimerString(seconds: Long): String {
        val times = SeparatorUtil.separateTime(seconds)
        return String.format(
            "%02d:%02d:%02d",
            times.first,
            times.second,
            times.third
        )
    }

    fun getCurrentPace(timeInMinutes: Double, distanceInKm: Double): Double {
        var pace = timeInMinutes / distanceInKm
        if (pace > 1000) pace = 999.0
        return pace
    }

}