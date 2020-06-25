package xevenition.com.runage.util

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.model.RunStats
import java.time.Instant
import java.util.concurrent.TimeUnit

object RunningUtil {

    fun getRunningTimer(startDateEpochSeconds: Long): Observable<String> {
        return Observable.interval(0, 1, TimeUnit.SECONDS)
            .map { Instant.now().epochSecond - startDateEpochSeconds }
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

    fun getPaceString(
        durationInSeconds: Long,
        distanceInMeters: Double,
        showAbbreviation: Boolean = true
    ): String {
        val secondsPerKm = durationInSeconds.toDouble() / (distanceInMeters / 1000)
        val times = SeparatorUtil.separateTime(secondsPerKm.toLong())
        var minutes = times.first * 60 + times.second
        //TODO fix imperial
        return if (minutes > 999) {
            if (showAbbreviation) {
                "999:00 min/km"
            } else {
                "999:00"
            }
        } else {
            val seconds = times.third
            if (showAbbreviation) {
                "$minutes:$seconds min/km"
            } else {
                "$minutes:$seconds"
            }
        }
    }

    @SuppressLint("CheckResult")
    fun processRunningStats(locations: List<PositionPoint>, locationUtil: LocationUtil): Single<RunStats> {
        val activityMap: MutableMap<Int, Int> = mutableMapOf()
        var distance = 0.0
        var duration = 0.0
        var lowest = locations.firstOrNull()?.altitude ?: 0.0
        var highest = locations.firstOrNull()?.altitude ?: 0.0
        return Observable.fromIterable(locations)
            .subscribeOn(Schedulers.computation())
            .map {
                if(it.altitude < lowest){
                    lowest = it.altitude
                }
                if(it.altitude > highest){
                    highest = it.altitude
                }
                it
            }
            .scan { t1: PositionPoint, t2: PositionPoint ->
                if (t2.activityType == DetectedActivity.RUNNING) {
                    distance += locationUtil.getDistanceBetweenPositionPoints(t2, t1)
                    duration += (t2.timeStampEpochSeconds - t1.timeStampEpochSeconds)
                }
                t2
            }
            .map {
                activityMap[it.activityType] = activityMap[it.activityType]?.plus(1) ?: 1
            }
            .toList()
            .map {
                val totalPoints = locations.size
                val activityPercentage = mutableMapOf<Int, Double>()
                activityPercentage[DetectedActivity.IN_VEHICLE] = activityMap.getOrDefault(
                    DetectedActivity.IN_VEHICLE, 0
                ).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.STILL] = activityMap.getOrDefault(
                    DetectedActivity.STILL, 0
                ).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.ON_BICYCLE] = activityMap.getOrDefault(
                    DetectedActivity.ON_BICYCLE, 0
                ).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.WALKING] = activityMap.getOrDefault(
                    DetectedActivity.WALKING, 0
                ).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.RUNNING] = activityMap.getOrDefault(
                    DetectedActivity.RUNNING, 0
                ).toDouble() / totalPoints.toDouble()

                val xp = LevelCalculator.getXpCalculation(duration, distance)
                val altitude = highest - lowest
                RunStats(xp, distance.toInt(), duration.toInt(), altitude.toInt(), activityPercentage)
            }
    }
}