package science.credo.mobiledetector.detector.camera2

import science.credo.mobiledetector.detector.BaseCalibrationResult
import science.credo.mobiledetector.utils.Statistics
import java.util.*

class RawFormatCalibrationResult(
    val clusterFactorWidth: Int,
    val clusterFactorHeight: Int,
    var detectionThreshold: Int,
    val calibrationNoise: Int,
    var thresholdAmplifier: Float = 1.10f
) : BaseCalibrationResult() {


    init {
        println("==========CALIBRATION RESULT amp $thresholdAmplifier")
    }
    //should be used to define need for recalibration
    val thresholdQueue: Deque<Int> = LinkedList()

    companion object {
        const val DEFAULT_NOISE_THRESHOLD = 10
    }

    fun adjustThreshold(max: Int) {
        thresholdQueue.addFirst((max * thresholdAmplifier).toInt())
        println("=====adjust threshold ${thresholdQueue.size} ")
        if (thresholdQueue.size > 20) {
            thresholdQueue.removeLast()
            detectionThreshold = Statistics(thresholdQueue.toIntArray()).mean.toInt()
        }
    }
}