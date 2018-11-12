package science.credo.mobiledetector

class GlobalDeviceState {
    object Calibration {
        /// Calibration is currently running
        @get:Synchronized @set:Synchronized
        var running = false

        // Time stamp of last calibration performed
        @get:Synchronized @set:Synchronized
        var last: Long = 0

        // When calibration is running, then it is timestamp of start of calibration
        @get:Synchronized @set:Synchronized
        var start: Long = 0

        // When calibration is running, then it contains count of frames used for calibrate
        @get:Synchronized @set:Synchronized
        var frames: Int = 0

        // When calibration is running, then it contains count of passed stages
        @get:Synchronized @set:Synchronized
        var stages: Int = 0

        var pixelAvg: FloatArray? = null
        var pixelMin: IntArray? = null
        var pixelMax: IntArray? = null

        var pixelFinalAvg: FloatArray? = null
        var pixelFinalAvgMin: FloatArray? = null
        var pixelFinalAvgMax: FloatArray? = null
        var pixelFinalMin: IntArray? = null
        var pixelFinalMax: IntArray? = null
    }

    object Detector {
        /// Detector is on
        @get:Synchronized @set:Synchronized
        var running = false

        /// Camera is on, detector is running
        @get:Synchronized @set:Synchronized
        var working = false

        /// Number of camera is run
        @get:Synchronized @set:Synchronized
        var cameraNumber: Int = 0
    }

    val detector = Detector
    val calibration = Calibration
}
