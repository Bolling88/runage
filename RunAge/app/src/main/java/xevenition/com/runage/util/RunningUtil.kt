package xevenition.com.runage.util

import android.annotation.SuppressLint
import android.location.Location
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
    fun calculateActivityDurationPercentage(location: List<PositionPoint>): Single<Map<Int, Double>> {
        val activityMap: MutableMap<Int, Int> = mutableMapOf()
        return Observable.fromIterable(location)
            .subscribeOn(Schedulers.computation())
            .map {
                activityMap[it.activityType] = activityMap[it.activityType]?.plus(1) ?: 1
            }
            .toList()
            .map {
                val totalPoints = location.size
                val activityPercentage = mutableMapOf<Int, Double>()
                activityPercentage[DetectedActivity.IN_VEHICLE] = activityMap.getOrDefault(
                    DetectedActivity.IN_VEHICLE, 0).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.STILL] = activityMap.getOrDefault(
                    DetectedActivity.STILL, 0).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.ON_BICYCLE] = activityMap.getOrDefault(
                    DetectedActivity.ON_BICYCLE, 0).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.WALKING] = activityMap.getOrDefault(
                    DetectedActivity.WALKING, 0).toDouble() / totalPoints.toDouble()
                activityPercentage[DetectedActivity.RUNNING] = activityMap.getOrDefault(
                    DetectedActivity.RUNNING, 0).toDouble() / totalPoints.toDouble()

                activityPercentage
            }
    }

    @SuppressLint("CheckResult")
    fun calculateExperience(locations: List<PositionPoint>): Single<Int> {
        var distance = 0.0
        var duration = 0.0
        return Observable.just(locations)
            .subscribeOn(Schedulers.computation())
            .map {
                val iterator = it.listIterator()
                for (lastPoint in iterator) {
                    if(iterator.hasNext()){
                        val nextPoint = iterator.next()
                        if(nextPoint.activityType == DetectedActivity.RUNNING){
                            val resultArray = FloatArray(4)
                            Location.distanceBetween(
                                lastPoint.latitude,
                                lastPoint.longitude,
                                nextPoint.latitude,
                                nextPoint.longitude,
                                resultArray
                            )
                            distance += resultArray.first()
                            duration += (nextPoint.timeStampEpochSeconds - lastPoint.timeStampEpochSeconds)
                        }
                    }
                }
            }
            .toList()
            .map {
                LevelCalculator.getXpCalculation(duration, distance)
            }
    }

}