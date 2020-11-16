package science.credo.mobiledetector2.analytics

import android.content.Context
import android.os.Bundle
import science.credo.mobiledetector2.detector.BaseCalibrationResult
import science.credo.mobiledetector2.settings.BaseSettings


class DetectorRunningEvent(
    context: Context,
    val timeInSeconds: Int,
    val detectionCount: Int,
    settings: BaseSettings,
    calibrationResult: BaseCalibrationResult
) : DetectorStateEvent(context,settings,calibrationResult){

    override val eventName: EventName
        get() = EventName.EVENT_DETECTOR_RUNNING

    override suspend fun createBundle(): Bundle {
        val bundle = super.createBundle()

        bundle.putInt(KEY_RUNNING_TIME, timeInSeconds)
        bundle.putInt(KEY_DETECTION_COUNT, detectionCount)
        bundle.putFloat(KEY_DETECTIONS_PER_H, detectionCount.toFloat() / (timeInSeconds / 3600F))

        return bundle
    }

}