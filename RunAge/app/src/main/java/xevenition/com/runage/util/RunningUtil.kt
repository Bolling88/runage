package xevenition.com.runage.util

import android.annotation.SuppressLint
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import xevenition.com.runage.model.PositionPoint
import java.time.Instant
import java.util.concurrent.TimeUnit

object RunningUtil {

    fun getRunningTimer(startDateEpochSeconds: Long): Observable<String> {
        return Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { Instant.now().epochSecond - startDateEpochSeconds}
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

    fun convertTimeToDurationStringMinutes(seconds: Long): String {
        val times = SeparatorUtil.separateTime(seconds)
        return String.format(
            "%02d:%02d",
            times.first,
            times.second
        )
    }

    fun getPaceString(durationInSeconds: Long, distanceInMeters: Double, showAbbreviation: Boolean = true): String {
        val secondsPerKm = durationInSeconds.toDouble() / (distanceInMeters/1000)
        val times = SeparatorUtil.separateTime(secondsPerKm.toLong())
        var minutes = times.first * 60 + times.second
        //TODO fix imperial
        return if(minutes > 999) {
            if(showAbbreviation) {
                "999:00 min/km"
            }else{
                "999:00"
            }
        }else {
            val seconds = times.third
            if(showAbbreviation) {
                "$minutes:$seconds min/km"
            }else{
                "$minutes:$seconds"
            }
        }
    }

    @SuppressLint("CheckResult")
    fun calculateActivityPercentage(location: List<PositionPoint>): Single<Map<Int, Int>> {
        val activityMap: MutableMap<Int, Int> = mutableMapOf()
        return Observable.fromIterable(location)
            .subscribeOn(Schedulers.computation())
            .map {
                activityMap[it.activityType] = activityMap[it.activityType]?.plus(1) ?: 1
            }
            .toList()
            .map {
                val totalPoints = location.size
                val activityPercentage = mutableMapOf<Int, Int>()
                activityPercentage[DetectedActivity.IN_VEHICLE] = activityMap.getOrDefault(
                    DetectedActivity.IN_VEHICLE, 0) / totalPoints
                activityPercentage[DetectedActivity.STILL] = activityMap.getOrDefault(
                    DetectedActivity.STILL, 0) / totalPoints
                activityPercentage[DetectedActivity.ON_BICYCLE] = activityMap.getOrDefault(
                    DetectedActivity.ON_BICYCLE, 0) / totalPoints
                activityPercentage[DetectedActivity.WALKING] = activityMap.getOrDefault(
                    DetectedActivity.WALKING, 0) / totalPoints
                activityPercentage[DetectedActivity.RUNNING] = activityMap.getOrDefault(
                    DetectedActivity.RUNNING, 0) / totalPoints

                var points = 0
                for(value in activityPercentage.values){
                    points += value
                }

                //even out the points
                if(points > 100){
                    activityPercentage[DetectedActivity.RUNNING] = activityPercentage.getOrDefault(DetectedActivity.RUNNING, 0) + (points - 100)
                }else{
                    activityPercentage[DetectedActivity.RUNNING] = activityPercentage.getOrDefault(DetectedActivity.RUNNING, 0) - (100 - points)
                }
                activityPercentage
            }
    }
}