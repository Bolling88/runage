package xevenition.com.runage.util

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.SystemClock
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import xevenition.com.runage.model.PositionPoint
import javax.inject.Inject


class LocationUtil @Inject constructor(private val app: Application) {
    private var kalmanNGLocationList = mutableListOf<Location>()
    private var kalmanFilter: KalmanLatLong = KalmanLatLong(3f)
    private var currentSpeed = 0.0f

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(
        locationRequest: LocationRequest?,
        locationCallback: LocationCallback
    ) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            app.mainLooper
        )
    }

    fun removeLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun kalmarFilter(location: Location, runStartTimeInMillis: Long): Boolean {
        /* Kalman Filter */
        /* Kalman Filter */
        val Qvalue: Float

        val elapsedTimeInMillis: Long =
            (location.elapsedRealtimeNanos / 1000000) - runStartTimeInMillis

        Qvalue = if (currentSpeed == 0.0f) {
            3.0f //3 meters per second
        } else {
            currentSpeed // meters per second
        }

        kalmanFilter.process(
            location.latitude,
            location.longitude,
            location.accuracy,
            elapsedTimeInMillis,
            Qvalue
        )
        val predictedLat: Double = kalmanFilter.getLat()
        val predictedLng: Double = kalmanFilter.getLng()

        val predictedLocation =
            Location("") //provider name is unecessary

        predictedLocation.latitude = predictedLat //your coords of course

        predictedLocation.longitude = predictedLng
        val predictedDeltaInMeters = predictedLocation.distanceTo(location)

        return if (predictedDeltaInMeters > 60) {
            Timber.d("Kalman Filter detects mal GPS, we should probably remove this from track")
            kalmanFilter.consecutiveRejectCount += 1
            if (kalmanFilter.consecutiveRejectCount > 3) {
                kalmanFilter =
                    KalmanLatLong(3f) //reset Kalman Filter if it rejects more than 3 times in raw.
            }
            kalmanNGLocationList.add(location)
            FirebaseCrashlytics.getInstance().setCustomKey("Kalmar filtered:", predictedDeltaInMeters)
            false
        } else {
            kalmanFilter.consecutiveRejectCount = 0
            true
        }
    }

    fun getLocationAge(newLocation: Location): Long {
        return (SystemClock.elapsedRealtimeNanos() / 1000000) - (newLocation.elapsedRealtimeNanos / 1000000)
    }

    fun getDistanceBetweenPositionPoints(lastPoint: PositionPoint, nextPoint: PositionPoint): Float {
        val resultArray = FloatArray(4)
        Location.distanceBetween(
            lastPoint.latitude,
            lastPoint.longitude,
            nextPoint.latitude,
            nextPoint.longitude,
            resultArray
        )
        return resultArray.first()
    }
}