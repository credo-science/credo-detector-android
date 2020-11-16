package science.credo.mobiledetector2.analytics

import android.content.Context
import android.os.Bundle
import science.credo.mobiledetector2.settings.BaseSettings
import science.credo.mobiledetector2.settings.Camera2ApiSettings
import science.credo.mobiledetector2.settings.CameraApi
import science.credo.mobiledetector2.settings.OldCameraSettings



class SettingsChangeEvent(
    context: Context,
    val settings: BaseSettings,
    val cameraApi: CameraApi
) : FirebaseEvent(context) {
    override val eventName: EventName
        get() = EventName.EVENT_SETTINGS_CHANGED

    override suspend fun createBundle(): Bundle {
        val bundle = Bundle()
        bundle.putInt(KEY_HEIGHT, settings.height)
        bundle.putInt(KEY_WIDTH, settings.width)
        bundle.putInt(KEY_IMAGE_FORMAT, settings.imageFormat)
        bundle.putString(KEY_API, cameraApi.name)
        when (settings) {
            is OldCameraSettings -> {
                bundle.putInt(KEY_FPS_RANGE_LOW, settings.fpsRange[0])
                bundle.putInt(KEY_FPS_RANGE_HIGH, settings.fpsRange[1])
            }
            is Camera2ApiSettings -> {
                bundle.putInt(KEY_ISO, settings.lowerIsoValue)
                bundle.putString(KEY_PROCESSING_METHOD, settings.processingMethod.name)
                bundle.putInt(KEY_EXPOSURE, settings.exposureInMillis)
                bundle.putString(KEY_CAMERA_ID, settings.cameraId)
            }
        }
        return bundle
    }

}