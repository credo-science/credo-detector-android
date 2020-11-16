package science.credo.mobiledetector2.detector.camera2

import science.credo.mobiledetector2.detector.BaseCalibrationResult
import science.credo.mobiledetector2.detector.BaseFrameResult
import java.lang.IllegalStateException

class RawFormatFrameResult (
    val avg: Int,
    val max: Int,
    val maxIndex: Int
) : BaseFrameResult() {


    companion object {
        fun fromJniStringData(data: String): RawFormatFrameResult {
            val parts = data.split(";")
            return RawFormatFrameResult(
                parts[0].toInt(),
                parts[1].toInt(),
                parts[2].toInt()
            )
        }
    }

    override fun isCovered(calibrationResult: BaseCalibrationResult?): Boolean {
        if (calibrationResult is RawFormatCalibrationResult?) {
            return avg <  RawFormatCalibrationResult.DEFAULT_NOISE_THRESHOLD
        } else {
            throw IllegalStateException()
        }
    }

}