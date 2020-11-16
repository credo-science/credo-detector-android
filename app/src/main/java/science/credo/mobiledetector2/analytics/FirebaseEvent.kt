package science.credo.mobiledetector2.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

const val KEY_WIDTH = "width"
const val KEY_HEIGHT = "height"
const val KEY_IMAGE_FORMAT = "image_format"
const val KEY_API = "camera_api"
const val KEY_FPS_RANGE_LOW = "fps_range_low"
const val KEY_FPS_RANGE_HIGH = "fps_range_high"
const val KEY_ISO = "iso"
const val KEY_PROCESSING_METHOD = "processing_method"
const val KEY_EXPOSURE = "exposure"
const val KEY_CAMERA_ID = "camera_id"
const val KEY_DETECTION_THRESHOLD = "detection_threshold"
const val KEY_DETECTION_INITIAL_THRESHOLD = "detection_initial_threshold"


abstract class FirebaseEvent(
    val context: Context
) {

    abstract val eventName: EventName

    abstract suspend fun createBundle(): Bundle

    suspend fun send() {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.logEvent(eventName.name.toLowerCase(), createBundle())
    }

    enum class EventName {
        EVENT_USER_REGISTRATION,
        EVENT_SETTINGS_CHANGED,
        EVENT_CAMERA2_RAW_DETECTOR_START,
        EVENT_OLD_DETECTOR_START,
        EVENT_DETECTOR_STOP,
        EVENT_DETECTOR_RUNNING,
        EVENT_CALIBRATION_INABILITY

    }
}


