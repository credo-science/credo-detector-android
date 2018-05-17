package science.credo.mobiledetector.events

data class StatsEvent(
        var frameWidth: Int = 0,
        var frameHeight: Int = 0,

        var startDetectionTimestamp: Long = 0,
        var activeDetection: Boolean = false,
        var lastFlushTimestamp: Long = 0,
        var lastFrameAchievedTimestamp: Long = 0,
        var lastFramePerformedTimestamp: Long = 0,
        var lastHitTimestamp: Long = 0,

        var allFrames: Int = 0,
        var performedFrames: Int = 0,
        var onTime: Long = 0,


        var blacksStats: StatsValue = StatsValue(),
        var averageStats: StatsValue = StatsValue(),
        var maxStats: StatsValue = StatsValue())
