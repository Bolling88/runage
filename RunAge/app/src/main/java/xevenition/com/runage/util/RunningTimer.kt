package xevenition.com.runage.util

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.time.Instant
import java.util.concurrent.TimeUnit

object RunningTimer {

    fun getRunningTimer(startDateMillis: Long): Observable<String> {
        val currentTimeMillis = Instant.now().epochSecond
        val initialValue = currentTimeMillis - startDateMillis
        return Observable.interval(0, 1, TimeUnit.MILLISECONDS)
            .map { it + initialValue }
            .map {
                convertMillisToTimerString(it)
            }
            .subscribeOn(Schedulers.computation())
    }

    fun convertMillisToTimerString(millis: Long): String {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}