package science.credo.credomobiledetektor.detection

import science.credo.credomobiledetektor.events.StatsEvent
import science.credo.credomobiledetektor.events.StatsValueBuilder

class DetectionStats {
    var lastFlushTimestamp = System.currentTimeMillis()
        private set

    private var lastFrameAchievedTimestamp = 0L
    private var lastFramePerformedTimestamp = 0L
    private var lastHitTimestamp = 0L

    private var lastCleansTimestamp = System.currentTimeMillis()
    private var allFrames = 0
    private var performedFrames = 0

    private var max = StatsValueBuilder()
    private var average = StatsValueBuilder()
    private var blacks = StatsValueBuilder()

    fun updateStats(max: Long, average: Double, zeroes: Double) {
        this.max.addSample(max.toDouble())
        this.average.addSample(average)
        blacks.addSample(zeroes)
    }

    fun frameAchieved() {
        allFrames++
        lastFrameAchievedTimestamp = System.currentTimeMillis()
    }

    fun framePerformed() {
        performedFrames++
        lastFramePerformedTimestamp = System.currentTimeMillis()
    }

    fun hitRegistered() {
        lastHitTimestamp = System.currentTimeMillis()
    }

    fun flush(stats : StatsEvent, cleanCounts : Boolean) {
        stats.lastFlushTimestamp = lastFlushTimestamp
        stats.lastFrameAchievedTimestamp = lastFrameAchievedTimestamp
        stats.lastFramePerformedTimestamp = lastFramePerformedTimestamp
        stats.lastHitTimestamp = lastHitTimestamp

        stats.maxStats = max.toStatsValue()
        stats.blacksStats = blacks.toStatsValue()
        stats.averageStats = average.toStatsValue()

        stats.allFrames = allFrames
        stats.performedFrames = performedFrames

        lastFlushTimestamp = System.currentTimeMillis()
        val period = lastFlushTimestamp - lastCleansTimestamp
        val fpms = allFrames.toDouble() / period.toDouble()
        stats.onTime = (performedFrames / fpms).toLong()

        if (cleanCounts) {
            lastCleansTimestamp = lastFlushTimestamp
            allFrames = 0
            performedFrames = 0
        }

        max = StatsValueBuilder()
        average = StatsValueBuilder()
        blacks = StatsValueBuilder()
    }
}