package science.credo.mobiledetector.detector.camera2

import science.credo.mobiledetector.detector.BaseCalibrationResult
import science.credo.mobiledetector.utils.Statistics
import java.util.*

class RawFormatCalibrationResult(
    val clusterFactorWidth: Int,
    val clusterFactorHeight: Int,
    var detectionThreshold: Int,
    val calibrationNoise: Int
) : BaseCalibrationResult() {

    //should be used to define need for recalibration
    val thresholdQueue: Deque<Int> = LinkedList()

    companion object {
        const val AMPLIFIER = 1.10
        const val DEFAULT_NOISE_THRESHOLD = 10
    }

    fun adjustThreshold(max: Int) {
        thresholdQueue.addFirst((max * AMPLIFIER).toInt())
        println("=====adjust threshold ${thresholdQueue.size} ")
        if (thresholdQueue.size > 20) {
            thresholdQueue.removeLast()
            detectionThreshold = Statistics(thresholdQueue.toIntArray()).mean.toInt()
        }
    }
}