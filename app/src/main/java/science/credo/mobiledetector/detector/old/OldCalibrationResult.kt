package science.credo.mobiledetector.detector.old

import android.content.Context
import science.credo.mobiledetector.detector.BaseCalibrationResult

class OldCalibrationResult(
    val blackThreshold: Int,
    val avg: Int,
    val max: Int
) : BaseCalibrationResult() {

    companion object{
        const val DEFAULT_BLACK_THRESHOLD = 40
    }
}