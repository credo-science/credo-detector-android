package science.credo.mobiledetector2.detector

import science.credo.mobiledetector2.detector.old.OldCalibrationResult
import java.lang.IllegalStateException


class OldFrameResult(
    val avg: Int,
    val blacksPercentage: Float,
    val max: Int,
    val maxIndex: Int
) :BaseFrameResult() {
    companion object {
        fun fromJniStringData(data: String, whiteLevel: Int?, blackLevelArray: IntArray?): OldFrameResult {
            val parts = data.split(";")
            println("====================== ${parts[1]}")
            if (whiteLevel == null || blackLevelArray == null) {
                return OldFrameResult(
                    parts[0].toInt(),
                    ((parts[1].toLong() * 10000L)/parts[2].toInt())/100f,
                    parts[3].toInt(),
                    parts[4].toInt()
                )
            } else {
                val maxPossible = whiteLevel - blackLevelArray.average()
                return OldFrameResult(
                    (parts[0].toInt() / maxPossible * 255.0).toInt(),
                    ((parts[1].toLong() * 10000L)/parts[2].toInt())/100f,
                    parts[3].toInt(),
                    parts[4].toInt()
                )
            }
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
