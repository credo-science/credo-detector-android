package science.credo.mobiledetector.info

import android.content.Context
import android.location.Location
import android.location.LocationManager
import science.credo.mobiledetector.database.ConfigurationWrapper

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

    private val mLastLocation: Location? = null
    private val cw: ConfigurationWrapper

    init {
        mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        cw = ConfigurationWrapper(mContext)
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

        if (loc != null) {
            cw.localizationLatitude = loc.latitude
            cw.localizationLongitude = loc.longitude
            cw.localizationAltitude = loc.altitude
            cw.localizationAccuracy = loc.accuracy
            cw.localizationProvider = loc.provider.toString()
            cw.localizationTimestamp = loc.time

            if (loc.latitude != 0.0 && loc.longitude != 0.0) {
                cw.localizationNeedUpdate = 0
            }
        }

        return LocationData(cw.localizationLatitude, cw.localizationLongitude, cw.localizationLatitude, cw.localizationAccuracy, cw.localizationProvider, cw.localizationTimestamp)
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