package science.credo.mobiledetector.detector

import science.credo.mobiledetector.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector.detector.old.OldCalibrationResult
import java.lang.IllegalStateException


class FrameResult(
    val avg: Int,
    val blacksPercentage: Float,
    val max: Int,
    val maxIndex: Int
) {
    companion object {
        fun fromJniStringData(data: String): FrameResult {
            val parts = data.split(";")
            return FrameResult(
                parts[0].toInt(),
                parts[1].toFloat(),
                parts[2].toInt(),
                parts[3].toInt()
            )
        }
    }

    fun isCovered(calibrationResult: BaseCalibrationResult?): Boolean {
        if (calibrationResult is OldCalibrationResult?) {
            return avg < calibrationResult?.avg ?: OldCalibrationResult.DEFAULT_BLACK_THRESHOLD &&
                    blacksPercentage >= 99.9
        } else if (calibrationResult is RawFormatCalibrationResult) {
            return avg < RawFormatCalibrationResult.DEFAULT_NOISE_THRESHOLD
        }
        throw IllegalStateException()
    }
}
