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

    companion object {
        val defaultEndpoint = "https://api.credo.science/api/v2"
    }
}
