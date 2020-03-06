package science.credo.mobiledetector.detector.old

import science.credo.mobiledetector.detector.FrameResult
import java.lang.Math.max
import java.lang.Math.min

class OldCalibrationFinder {

    companion object {
        const val CALIBRATION_LENGHT = 500
    }

    var counter = 0
    var avgSum = 0L
    var maxSum = 0L
    var blacksPercentageSum = 0f

    suspend fun nextFrame(frameResult: FrameResult): OldCalibrationResult? {
        avgSum += frameResult.avg
        blacksPercentageSum += frameResult.blacksPercentage
        maxSum += frameResult.max
        counter++
        return if (counter >= CALIBRATION_LENGHT) {
            val avg = avgSum / CALIBRATION_LENGHT
            val blacksPercentage = blacksPercentageSum / CALIBRATION_LENGHT
            val max_ = maxSum / CALIBRATION_LENGHT
            val finalAvg = (avg + 20).coerceAtMost(60).coerceAtLeast(10)
//            val finalBlack = (blacksPercentage + 20).coerceAtMost(60f).coerceAtLeast(10f)
            val finalBlack = (avg + 20).coerceAtMost(60).coerceAtLeast(10)

            val finalMax =
                (max_ * 3).coerceAtLeast(80).coerceAtMost(160).coerceAtLeast(finalAvg)
            OldCalibrationResult(
                finalBlack.toInt(),
                finalAvg.toInt(),
                finalMax.toInt()
            )

        } else {
            null
        }
    }

}