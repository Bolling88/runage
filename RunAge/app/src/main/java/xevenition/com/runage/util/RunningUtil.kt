package xevenition.com.runage.util

import android.annotation.SuppressLint
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import xevenition.com.runage.R
import xevenition.com.runage.model.PositionPoint
import xevenition.com.runage.model.RunStats
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RunningUtil @Inject constructor(
    private val resourceUtil: ResourceUtil,
    private val saveUtil: SaveUtil
) {

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

    fun getDistanceString(distance: Int): String {
        return if (saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)) {
            if (distance < 100) {
                "$distance m"
            } else {
                "${"%.2f".format(distance.toDouble() / 1000)} ${resourceUtil.getString(R.string.runage_km)}"
            }
        } else {
            if (distance < 160.9344) {
                "${"%.2f".format(distance.toDouble() * 3.2808)} ft"
            } else {
                "${"%.2f".format(distance.toDouble() / 1609.344)} ${resourceUtil.getString(R.string.runage_mi)}"
            }
        }
    }

    fun convertPoundsToKilograms(pounds: Double): Double {
        return pounds / 2.2046
    }

    fun getSecondsBehindCheckpoint(
        durationInSeconds: Long,
        distanceInMeters: Double,
        targetDistance: Int
    ): Int {
        val metersLeft: Double = targetDistance - distanceInMeters
        val metersPerSecond: Double = distanceInMeters / durationInSeconds.toDouble()
        val secondsBehind = metersLeft / metersPerSecond
        return secondsBehind.toInt()
    }

    fun getPaceString(
        durationInSeconds: Long,
        distanceInMeters: Double,
        showAbbreviation: Boolean = true
    ): String {
        val isMetric = saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)
        val kmEnding = resourceUtil.getString(R.string.runage_min_km)
        val miEnding = resourceUtil.getString(R.string.runage_min_mi)
        val secondsPer: Double = if (isMetric) {
            durationInSeconds.toDouble() / (distanceInMeters / 1000)
        } else {
            durationInSeconds.toDouble() / (distanceInMeters / 1609.344)
        }
        val times = SeparatorUtil.separateTime(secondsPer.toLong())
        val minutes = times.first * 60 + times.second
        return if (minutes > 999) {
            if (showAbbreviation) {
                if (isMetric) {
                    "999:00 $kmEnding"
                } else {
                    "999:00 $miEnding"
                }
            } else {
                "999:00"
            }
        } else {
            val seconds = times.third
            if (showAbbreviation) {
                if (isMetric) {
                    String.format("%d:%02d", minutes, seconds) + " " + kmEnding
                } else {
                    String.format("%d:%02d", minutes, seconds) + " " + miEnding
                }
            } else {
                String.format("%d:%02d", minutes, seconds)
                "$minutes:$seconds"
            }
        }
    }

    @SuppressLint("CheckResult")
    fun processRunningStats(
        locations: List<PositionPoint>,
        locationUtil: LocationUtil
    ): Single<RunStats> {
        val activityMap: MutableMap<Int, Int> = mutableMapOf()
        var distance = 0.0
        var duration = 0.0
        var lowest = locations.firstOrNull()?.altitude ?: 0.0
        var highest = locations.firstOrNull()?.altitude ?: 0.0
        return Observable.fromIterable(locations)
            .subscribeOn(Schedulers.computation())
            .map {
                if (it.altitude < lowest) {
                    lowest = it.altitude
                }
                if (it.altitude > highest) {
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
                RunStats(
                    xp,
                    distance.toInt(),
                    duration.toInt(),
                    altitude.toInt(),
                    activityPercentage
                )
            }
    }
}