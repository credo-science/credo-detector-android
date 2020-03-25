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
            println("====================== ${parts[1]}")
            return OldFrameResult(
                parts[0].toInt(),
                ((parts[1].toLong() * 10000L)/parts[2].toInt())/100f,
                parts[3].toInt(),
                parts[4].toInt()
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
