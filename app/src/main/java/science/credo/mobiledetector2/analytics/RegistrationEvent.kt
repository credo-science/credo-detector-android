package science.credo.mobiledetector2.analytics

import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

const val KEY_SUPPORTED_FORMATS = "old_supported_formats"
const val KEY_MIN_WIDTH = "old_min_width"
const val KEY_MIN_HEIGHT = "old_min_height"
const val KEY_MAX_WIDTH = "old_max_width"
const val KEY_MAX_HEIGHT = "old_max_height"
const val KEY_FPS_MIN = "old_fps_min"
const val KEY_FPS_MAX = "old_fps_max"
const val KEY_RAW_BACK_WIDTH = "raw_back_width"
const val KEY_RAW_BACK_HEIGHT = "raw_back_height"
const val KEY_RAW_FRONT_WIDTH = "raw_front_width"
const val KEY_RAW_FRONT_HEIGHT = "raw_front_height"
const val KEY_RAW_BACK_MIN_EXPOSURE = "raw_back_min_exposure"
const val KEY_RAW_FRONT_MIN_EXPOSURE = "raw_front_min_exposure"

class RegistrationEvent(
    context: Context
) : FirebaseEvent(context) {

    override suspend fun createBundle(): Bundle {
        return GlobalScope.async {
            val camera = Camera.open()
            camera.lock()
            val parameters = camera.parameters
            camera.unlock()
            camera.release()
            val bundle = Bundle()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val cameraManager =
                    context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                for (cameraId in cameraManager.cameraIdList) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val scalerMap =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    if (scalerMap?.outputFormats?.contains(32) == true) {
                        val size = scalerMap.getOutputSizes(32)[0]
                        val minFrameDuration = scalerMap.getOutputMinFrameDuration(32, size)
                        val minInMillis = (minFrameDuration / 1000000)
                        when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                            CameraCharacteristics.LENS_FACING_BACK -> {
                                bundle.putInt(KEY_RAW_BACK_WIDTH, size.width)
                                bundle.putInt(KEY_RAW_BACK_HEIGHT, size.height)
                                bundle.putLong(KEY_RAW_BACK_MIN_EXPOSURE, minInMillis)
                            }
                            CameraCharacteristics.LENS_FACING_FRONT -> {
                                bundle.putInt(KEY_RAW_FRONT_WIDTH, size.width)
                                bundle.putInt(KEY_RAW_FRONT_HEIGHT, size.height)
                                bundle.putLong(KEY_RAW_FRONT_MIN_EXPOSURE, minInMillis)
                            }
                        }
                    }
                }
            }

            var minSize: Camera.Size? = null
            var maxSize: Camera.Size? = null
            var minSizeValue: Long = Long.MAX_VALUE
            var maxSizeValue: Long = 0
            for (size in parameters.supportedPreviewSizes) {
                val v = (size.height * size.width).toLong()
                if (v < minSizeValue) {
                    minSizeValue = v
                    minSize = size
                }
                if (v > maxSizeValue) {
                    maxSizeValue = v
                    maxSize = size
                }
            }
            var minFps: Int = Int.MAX_VALUE
            var maxFps: Int = 0
            for (range in parameters.supportedPreviewFpsRange) {
                if (range[0] < minFps) {
                    minFps = range[0]
                }
                if (range[1] > maxFps) {
                    maxFps = range[1]
                }
            }

            bundle.putIntArray(
                KEY_SUPPORTED_FORMATS,
                parameters.supportedPreviewFormats.toIntArray()
            )
            bundle.putInt(KEY_MIN_HEIGHT, minSize?.height ?: 0)
            bundle.putInt(KEY_MIN_WIDTH, minSize?.width ?: 0)
            bundle.putInt(KEY_MAX_HEIGHT, maxSize?.height ?: 0)
            bundle.putInt(KEY_MAX_WIDTH, maxSize?.width ?: 0)
            bundle.putInt(KEY_FPS_MIN, minFps)
            bundle.putInt(KEY_FPS_MAX, maxFps)
            return@async bundle
        }.await()
    }

    override val eventName: EventName
        get() = EventName.EVENT_USER_REGISTRATION
}