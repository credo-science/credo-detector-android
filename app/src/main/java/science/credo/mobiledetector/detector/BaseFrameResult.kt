package science.credo.mobiledetector.detector

abstract class BaseFrameResult() {
    abstract fun isCovered(calibrationResult: BaseCalibrationResult?) :Boolean
}