package science.credo.mobiledetector2.detector.camera2

import science.credo.mobiledetector2.detector.BaseCalibrationResult
import science.credo.mobiledetector2.utils.Statistics
import java.util.*

class RawFormatCalibrationResult(
    val clusterFactorWidth: Int,
    val clusterFactorHeight: Int,
    var detectionThreshold: Int,
    val calibrationNoise: Int
) : BaseCalibrationResult() {


    val initialDetectionThreshold :Int = detectionThreshold

    //should be used to define need for recalibration
    val thresholdQueue: Deque<Int> = LinkedList()

    companion object {
        const val DEFAULT_NOISE_THRESHOLD = 10
    }

    fun adjustThreshold(max: Int, thresholdAmplifier: Float) {
        thresholdQueue.addFirst((max * thresholdAmplifier).toInt())
        if (thresholdQueue.size > 20) {
            thresholdQueue.removeLast()
            detectionThreshold = Statistics(thresholdQueue.toIntArray()).mean.toInt()
        }
    }
}