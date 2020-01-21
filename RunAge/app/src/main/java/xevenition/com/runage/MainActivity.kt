package xevenition.com.runage

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACTIVITY_RECOGNITION
                )
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACTIVITY_RECOGNITION
                    )
            ) {
                //TODO
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                                Manifest.permission.ACTIVITY_RECOGNITION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ACCESS_BACKGROUND_LOCATION
                        ),
                        MY_PERMISSIONS_REQUEST
                )
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                                Manifest.permission.ACTIVITY_RECOGNITION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        MY_PERMISSIONS_REQUEST
                )
            }
        } else {
            // Permission has already been granted
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    companion object {
        const val MY_PERMISSIONS_REQUEST = 3242
    }
}
