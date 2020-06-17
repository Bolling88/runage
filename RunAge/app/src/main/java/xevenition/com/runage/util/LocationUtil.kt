package xevenition.com.runage.util

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import javax.inject.Inject


class LocationUtil @Inject constructor(private val app: Application) {
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
}