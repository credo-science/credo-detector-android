package science.credo.mobiledetector2.analytics

import android.content.Context
import science.credo.mobiledetector2.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector2.settings.Camera2ApiSettings


class RawDetectorStartEvent(
    context: Context,
    settings: Camera2ApiSettings,
    calibrationResult: RawFormatCalibrationResult
) : DetectorStateEvent(context,settings,calibrationResult) {
    override val eventName: EventName
        get() = EventName.EVENT_CAMERA2_RAW_DETECTOR_START


}