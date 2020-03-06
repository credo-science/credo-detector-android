package science.credo.mobiledetector.detector.camera2

class RawFormatCalibrationResult(
    clusterFactorWidth: Int,
    clusterFactorHeight: Int,
    detectionThreshold: Int,
    avgNoise: Int
){
    companion object{
        const val AMPLIFIER = 1.05
    }
}