package science.credo.mobiledetector2.detector.old

import science.credo.mobiledetector2.detector.OldFrameResult

class OldCalibrationFinder {

    companion object {
        const val CALIBRATION_LENGHT = 500
    }

    var counter = 0
    var avgSum = 0L
    var maxSum = 0L
    var blacksPercentageSum = 0f

    suspend fun nextFrame(oldFrameResult: OldFrameResult): OldCalibrationResult? {
        avgSum += oldFrameResult.avg
        blacksPercentageSum += oldFrameResult.blacksPercentage
        maxSum += oldFrameResult.max
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

            val result = OldCalibrationResult(
                finalBlack.toInt(),
                finalAvg.toInt(),
                finalMax.toInt()
            )
            result.avgAvg = avg
            result.avgMax = max_
            result.avgBlacksPercentage = blacksPercentage

            return result

        } else {
            null
        }
    }

}