package science.credo.mobiledetector.detector.camera2

import science.credo.mobiledetector.detector.BaseCalibrationResult

class RawFormatCalibrationResult(
    val clusterFactorWidth: Int,
    val clusterFactorHeight: Int,
    val detectionThreshold: Int,
    val avgNoise: Int
) : BaseCalibrationResult() {
    companion object {
        const val AMPLIFIER = 1.05
        const val DEFAULT_NOISE_THRESHOLD = 1.05

    }
}