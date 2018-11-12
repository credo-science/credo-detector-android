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
            return preferences.getInt("camera_number", 0)
        }
        set(v) {
            setInt("camera_number", v)
        }

    var detectionAlgorithm: Int
        get() {
            return preferences.getInt("detection_algorithm", 2)
        }
        set(v) {
            setInt("detection_algorithm", v)
        }

    var preferedWidth: Int
        get() {
            return preferences.getInt("prefered_width", 1280)
        }
        set(v) {
            setInt("prefered_width", v)
        }

    var preferedHeight: Int
        get() {
            return preferences.getInt("prefered_height", 720)
        }
        set(v) {
            setInt("prefered_height", v)
        }

    var calibrationFrequency: Int
        get() {
            return preferences.getInt("calibration_frequency", 20 * 60 * 1000)
        }
        set(v) {
            setInt("calibration_frequency", v)
        }

    var calibrationFrames: Int
        get() {
            return preferences.getInt("calibration_frames", 100)
        }
        set(v) {
            setInt("calibration_frames", v)
        }

    var calibrationStages: Int
        get() {
            return preferences.getInt("calibration_stages", 10)
        }
        set(v) {
            setInt("calibration_stages", v)
        }

    companion object {
        val defaultEndpoint = "https://api.credo.science/api/v2"
    }
}
