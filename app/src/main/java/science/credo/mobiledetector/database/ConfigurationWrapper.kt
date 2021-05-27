package science.credo.mobiledetector.database

import android.content.Context
import android.preference.PreferenceManager

class ConfigurationWrapper(context: Context) : SharedPreferencesWrapper(context) {
    var endpoint : String
        get() {
            val v = preferences.getString("endpoint", defaultEndpoint)
            if (v == null || v.trim().isEmpty()) {
                return defaultEndpoint
            } else {
                return v
            }
        }
        set(v) {
            setString("endpoint", v)
        }

    var dbSchema : String
        get() {
            return preferences.getString("db_schema", "")!!
        }
        set(v) {
            setString("db_schema", v)
        }

    var autoCalibrationPerformed: Boolean
        get() {
            return preferences.getBoolean("auto_calibration", true)
        }
        set(v) {
            setBoolean("auto_calibration", v)
        }

    var autoRun: Boolean
        get() {
            return preferences.getBoolean("auto_run", true)
        }
        set(v) {
            setBoolean("auto_run", v)
        }

    var cameraNumber: Int
        get() {
            return preferences.getString("camera_number", "0")!!.toInt()
        }
        set(v) {
            setString("camera_number", "$v")
        }

    var frameSize: Int
        get() {
            return preferences.getString("frame_size", "0")!!.toInt()
        }
        set(v) {
            setString("frame_size", "$v")
        }

    var localizationLatitude: Double
        get() {
            return preferences.getString("latitude", "0")!!.toDouble()
        }
        set(v) {
            setString("latitude", "$v")
        }

    var localizationLongitude: Double
        get() {
            return preferences.getString("longitude", "0")!!.toDouble()
        }
        set(v) {
            setString("longitude", "$v")
        }

    var localizationAltitude: Double
        get() {
            return preferences.getString("altitude", "0")!!.toDouble()
        }
        set(v) {
            setString("altitude", "$v")
        }

    var localizationAccuracy: Float
        get() {
            return preferences.getString("accuracy", "0")!!.toFloat()
        }
        set(v) {
            setString("accuracy", "$v")
        }

    var localizationProvider: String
        get() {
            return preferences.getString("provider", "")!!
        }
        set(v) {
            setString("provider", v)
        }

    var localizationTimestamp: Long
        get() {
            return preferences.getString("timestamp", "0")!!.toLong()
        }
        set(v) {
            setString("timestamp", "$v")
        }

    var localizationIP: String
        get() {
            return preferences.getString("ip", "")!!
        }
        set(v) {
            setString("ip", v)
        }

    var localizationNeedUpdate: Int // 0 - ok, 1 - should, 2 - must
        get() {
            return preferences.getInt("location_need_update", 2)
        }
        set(v) {
            setInt("location_need_update", v)
        }

    companion object {
        val defaultEndpoint = "https://api.credo.science/api/v2"
    }
}
