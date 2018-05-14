package science.credo.mobiledetector.info

import android.content.Context
import android.location.Location
import android.location.LocationManager

/**
 * Created by poznan on 25/08/2017.
 */


class LocationInfo(mContext: Context) {

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val accuracy: Float,
        val provider: String,
        val timestamp: Long
    ) {
    }

    private var mLastLocation: Location? = null

    init {
        mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun getGpsLocation(): Location? {
        return mLocationManager?.getLastKnownLocation(mGpsProvider)
    }

    fun getNetworkLocation(): Location? {
        return mLocationManager?.getLastKnownLocation(mNetworkProvider)
    }

    fun getLocation(): Location? = getGpsLocation() ?: getNetworkLocation()

    fun getLocationData(): LocationData {
        val loc = getLocation()
        val time = System.currentTimeMillis()
        return if (loc == null) LocationData(0.0, 0.0, 0.0, 0.0f, "none", time)
        else LocationData(
            loc.latitude,
            loc.longitude,
            loc.altitude,
            loc.accuracy,
            loc.provider,
            time
        )
    }

    fun getLocationString(): String = getLocationData().toString()

    companion object {
        val mGpsProvider = LocationManager.GPS_PROVIDER
        val mNetworkProvider = LocationManager.NETWORK_PROVIDER
        private var mLocationManager: LocationManager? = null;

        private var mLocationInfo: LocationInfo? = null;

        fun getInstance(context: Context): LocationInfo {
            if (mLocationInfo == null) {
                mLocationInfo = LocationInfo(context)
            }
            return mLocationInfo!!
        }
    }
}