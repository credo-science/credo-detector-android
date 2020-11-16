package science.credo.mobiledetector2.detector

abstract class BaseFrameResult() {
    abstract fun isCovered(calibrationResult: BaseCalibrationResult?) :Boolean
}