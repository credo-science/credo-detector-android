package science.credo.mobiledetector2.detector.old

import science.credo.mobiledetector2.detector.BaseCalibrationResult

class OldCalibrationResult(
    val blackThreshold: Int,
    val avg: Int,
    val max: Int
) : BaseCalibrationResult() {

    var avgMax = -1L
    var avgBlacksPercentage = -1f
    var avgAvg = -1L

    companion object {
        const val DEFAULT_BLACK_THRESHOLD = 40
    }
}