package science.credo.mobiledetector2.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat

object LocationHelper : LocationListener {

    var location: Location? = null
    private var updateTime = 0L

    fun init(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            location = null
        } else {
            val locationManager = context.getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager
            var bestTime: Long = 0
            var bestAccuracy = java.lang.Float.MAX_VALUE
            val matchingProviders = locationManager.allProviders
            for (provider in matchingProviders) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    if (location.time > updateTime) {
                        updateTime = location.time
                        this.location = location
                    }
                }
                if (location == null) {
                    val locCriteria = Criteria()
                    locCriteria.accuracy = Criteria.ACCURACY_FINE
                    locationManager.requestSingleUpdate(locCriteria, this, context.mainLooper)
                }
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                900000,
                5f,
                this
            )
        }
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            if (location.time > updateTime) {
                updateTime = location.time
                this@LocationHelper.location = location
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }


}