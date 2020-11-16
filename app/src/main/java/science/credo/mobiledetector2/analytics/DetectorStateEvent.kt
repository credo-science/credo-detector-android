package science.credo.mobiledetector2.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import science.credo.mobiledetector2.detector.BaseCalibrationResult
import science.credo.mobiledetector2.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector2.detector.old.OldCalibrationResult
import science.credo.mobiledetector2.settings.BaseSettings
import science.credo.mobiledetector2.settings.Camera2ApiSettings
import science.credo.mobiledetector2.settings.OldCameraSettings
import science.credo.mobiledetector2.utils.BatteryStateReceiver
import science.credo.mobiledetector2.utils.SensorHelper

const val KEY_RUNNING_TIME = "running_time"
const val KEY_DETECTION_COUNT = "detection_count"
const val KEY_DETECTIONS_PER_H = "detections_per_h"
const val KEY_AVG = "cluster_height_factor"
const val KEY_BLACK = "calibration_noise"
const val KEY_CLUSTER_FACTOR_WIDTH = "cluster_width_factor"
const val KEY_CLUSTER_FACTOR_HEIGHT = "cluster_height_factor"
const val KEY_CALIBRATION_NOISE = "calibration_noise"

const val KEY_AMBIENT_TEMPERATURE = "detections_per_h"
const val KEY_BATTERY_TEMPERATURE = "detections_per_h"

abstract class DetectorStateEvent(
    context: Context,
    val settings: BaseSettings,
    val calibrationResult: BaseCalibrationResult?
) : FirebaseEvent(context){

    init {
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_HEIGHT, settings.height)
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_WIDTH, settings.width)
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_IMAGE_FORMAT, settings.imageFormat)
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_AMBIENT_TEMPERATURE,SensorHelper.temperature)
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_BATTERY_TEMPERATURE,BatteryStateReceiver.lastKnownTemperature?:0)
        when (settings) {
            is OldCameraSettings -> {
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_FPS_RANGE_LOW, settings.fpsRange[0])
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_FPS_RANGE_HIGH, settings.fpsRange[1])
            }
            is Camera2ApiSettings -> {
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_ISO, settings.lowerIsoValue)
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_PROCESSING_METHOD, settings.processingMethod.name)
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_EXPOSURE, settings.exposureInMillis)
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_CAMERA_ID, settings.cameraId)
            }
        }
        when (calibrationResult) {
            is OldCalibrationResult -> {
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_DETECTION_THRESHOLD, calibrationResult.max)
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_BLACK, calibrationResult.blackThreshold)
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_AVG, calibrationResult.avg)
            }
            is RawFormatCalibrationResult -> {
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_CLUSTER_FACTOR_HEIGHT, calibrationResult.clusterFactorHeight)
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_CLUSTER_FACTOR_WIDTH, calibrationResult.clusterFactorWidth)
                FirebaseCrashlytics.getInstance().setCustomKey(KEY_CALIBRATION_NOISE, calibrationResult.calibrationNoise)
                FirebaseCrashlytics.getInstance().setCustomKey(
                    KEY_DETECTION_THRESHOLD,
                    calibrationResult.detectionThreshold
                )
                FirebaseCrashlytics.getInstance().setCustomKey(
                    KEY_DETECTION_INITIAL_THRESHOLD,
                    calibrationResult.initialDetectionThreshold
                )
            }
        }
    }

    override suspend fun createBundle(): Bundle {
        val bundle = Bundle()
        bundle.putInt(KEY_HEIGHT, settings.height)
        bundle.putInt(KEY_WIDTH, settings.width)
        bundle.putInt(KEY_IMAGE_FORMAT, settings.imageFormat)
        bundle.putInt(KEY_AMBIENT_TEMPERATURE,SensorHelper.temperature)
        bundle.putInt(KEY_BATTERY_TEMPERATURE,BatteryStateReceiver.lastKnownTemperature?:0)

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

        when (calibrationResult) {
            is OldCalibrationResult -> {
                bundle.putInt(KEY_DETECTION_THRESHOLD, calibrationResult.max)
                bundle.putInt(KEY_BLACK, calibrationResult.blackThreshold)
                bundle.putInt(KEY_AVG, calibrationResult.avg)
            }
            is RawFormatCalibrationResult -> {
                bundle.putInt(KEY_CLUSTER_FACTOR_HEIGHT, calibrationResult.clusterFactorHeight)
                bundle.putInt(KEY_CLUSTER_FACTOR_WIDTH, calibrationResult.clusterFactorWidth)
                bundle.putInt(KEY_CALIBRATION_NOISE, calibrationResult.calibrationNoise)
                bundle.putInt(
                    KEY_DETECTION_THRESHOLD,
                    calibrationResult.detectionThreshold
                )
                bundle.putInt(
                    KEY_DETECTION_INITIAL_THRESHOLD,
                    calibrationResult.initialDetectionThreshold
                )
            }
        }
        return bundle
    }

}