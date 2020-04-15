package science.credo.mobiledetector.settings

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraMetadata
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.Expose

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2ApiSettings(
    format: Int,
    width: Int,
    height: Int,
    val cameraId: String,
    val exposureInMillis: Int,
    val processingMethod: ProcessingMethod
) : BaseSettings(
    format,
    width,
    height
) {
    var sendToServer: Boolean = true
    var saveToMemory: Boolean = false
    var saveFrameByteArraySource: Boolean = false
    var scaledWidth: Int = 0
    var scaledHeight: Int = 0
    var template = CameraDevice.TEMPLATE_PREVIEW
    var lowerIsoValue: Int = -1
    //    var sensorExposureTime: Long = -1L
//    var sensorMaxExposureTime: Long = -1L
//    var frameDuration: Long =-1L
    var controlMode: Int = CameraMetadata.CONTROL_MODE_OFF
    val controlAEMode: Int = CameraMetadata.CONTROL_AE_MODE_OFF
    var thresholdMultiplier :Float? = 1.10f
    val maxImages: Int = 2
}

