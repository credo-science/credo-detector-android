package science.credo.mobiledetector.detector

import science.credo.mobiledetector.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector.detector.old.OldCalibrationResult
import java.lang.IllegalStateException


class OldFrameResult(
    val avg: Int,
    val blacksPercentage: Float,
    val max: Int,
    val maxIndex: Int
) :BaseFrameResult() {
    companion object {
        fun fromJniStringData(data: String): OldFrameResult {
            val parts = data.split(";")
            return OldFrameResult(
                parts[0].toInt(),
                parts[1].toFloat()/100,
                parts[2].toInt(),
                parts[3].toInt()
            )
        }
    }

   override fun isCovered(calibrationResult: BaseCalibrationResult?): Boolean {
        if (calibrationResult is OldCalibrationResult?) {
            println("=======================avg avg   $avg   ${calibrationResult?.avg ?: OldCalibrationResult.DEFAULT_BLACK_THRESHOLD}   $blacksPercentage")
            return avg < calibrationResult?.avg ?: OldCalibrationResult.DEFAULT_BLACK_THRESHOLD &&
                    blacksPercentage >= 99.9
        } else{
            throw IllegalStateException()
        }
    }

}
