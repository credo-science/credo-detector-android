package science.credo.mobiledetector.detector.camera2

import science.credo.mobiledetector.detector.BaseCalibrationResult

class RawFormatCalibrationResult(
    val clusterFactorWidth: Int,
    val clusterFactorHeight: Int,
    val detectionThreshold: Int,
    val calibrationNoise : Int
) : BaseCalibrationResult() {

     //should be used to define need for recalibration

    companion object {
        const val AMPLIFIER = 1.15
        const val DEFAULT_NOISE_THRESHOLD = 10

    }
}