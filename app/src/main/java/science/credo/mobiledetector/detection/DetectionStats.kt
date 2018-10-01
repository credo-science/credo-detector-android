package science.credo.mobiledetector.detection

import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.events.StatsValueBuilder
import kotlin.math.min

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

    private var detection = false
    //private var oldDetection = false
    private var detectionTimestamp = 0L // TODO: rename to frameTimestamp
    private var oldDetectionTimestamp = 0L // TODO: rename to oldFrameTimestamp
    private var onTime = 0L

    fun updateStats(max: Int, average: Double, zeroes: Double) {
        this.max.addSample(max.toDouble())
        this.average.addSample(average)
        blacks.addSample(zeroes)
    }

    fun activeDetect(detectionOn:Boolean, timestamp: Long) { // TODO: detectionOn -> gccFilterResult, activeDetect -> assumeOnTime
        //oldDetection = detection
        oldDetectionTimestamp = detectionTimestamp
        this.detection = detectionOn
        detectionTimestamp = timestamp

        // no add to onTime when gccFilterResult is false or oldFrameTimestamp is not initialized
        if (detection && oldDetectionTimestamp != 0L) {
            onTime += min(detectionTimestamp - oldDetectionTimestamp, 1000)
        }
    }

    fun frameAchieved(timestamp: Long) {
        allFrames++
        lastFrameAchievedTimestamp = timestamp
    }

    fun framePerformed(timestamp: Long) {
        performedFrames++
        lastFramePerformedTimestamp = timestamp
    }

    fun hitRegistered(timestamp: Long) {
        lastHitTimestamp = timestamp
    }

    fun flush(stats : StatsEvent, cleanCounts : Boolean, timestamp: Long) {
        stats.lastFlushTimestamp = lastFlushTimestamp
        stats.lastFrameAchievedTimestamp = lastFrameAchievedTimestamp
        stats.lastFramePerformedTimestamp = lastFramePerformedTimestamp
        stats.lastHitTimestamp = lastHitTimestamp

        stats.maxStats = max.toStatsValue()
        stats.blacksStats = blacks.toStatsValue()
        stats.averageStats = average.toStatsValue()

        stats.allFrames = allFrames
        stats.performedFrames = performedFrames

        stats.activeDetection = detection

        lastFlushTimestamp = timestamp
        //val period = lastFlushTimestamp - lastCleansTimestamp
        //val fpms = allFrames.toDouble() / period.toDouble()
        stats.onTime = onTime//(performedFrames / fpms).toLong()

        if (cleanCounts) {
            lastCleansTimestamp = lastFlushTimestamp
            allFrames = 0
            performedFrames = 0
            onTime = 0L
        }

        max = StatsValueBuilder()
        average = StatsValueBuilder()
        blacks = StatsValueBuilder()
    }
}