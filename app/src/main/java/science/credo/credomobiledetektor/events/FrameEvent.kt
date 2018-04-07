package science.credo.credomobiledetektor.events

data class FrameEvent(
        var width: Int = 0,
        var height: Int = 0,
        var startDetection: Long = 0,
        var lastUpdate: Long = 0,
        var lastHit: Long = 0,
        var frames: Int = 0,
        var max: Long = 0,
        var average: Double = 0.0,
        var zeros: Double = 0.0,
        var sum: Long = 0)
