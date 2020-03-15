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

    fun convertMillisToTimerString(millis: Long): String {
        return String.format("%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(millis),
            TimeUnit.SECONDS.toMinutes(millis) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(millis)),
            TimeUnit.SECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(millis)));
    }
}