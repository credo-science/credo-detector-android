package science.credo.mobiledetector.detection

import com.instacart.library.truetime.TrueTime
import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.events.StatsValueBuilder
import kotlin.math.min

class DetectionStats {
    var lastFlushTimestamp = TrueTime.now().time
        private set

    private var lastFrameAchievedTimestamp = 0L
    private var lastFramePerformedTimestamp = 0L
    private var lastHitTimestamp = 0L

    private var lastCleansTimestamp = TrueTime.now().time
    private var allFrames = 0
    private var performedFrames = 0

    private var max = StatsValueBuilder()
    private var average = StatsValueBuilder()
    private var blacks = StatsValueBuilder()

    private var detection = false
    private var oldDetection = false
    private var detectionTimestamp = 0L
    private var oldDetectionTimestamp = 0L
    private var onTime = 0L

    fun updateStats(max: Int, average: Double, zeroes: Double) {
        this.max.addSample(max.toDouble())
        this.average.addSample(average)
        blacks.addSample(zeroes)
    }

    fun activeDetect(detectionOn:Boolean) {
        oldDetection = detection
        oldDetectionTimestamp = detectionTimestamp
        this.detection=detectionOn
        detectionTimestamp = System.currentTimeMillis()
        if (oldDetection && detection) {
            onTime += min(detectionTimestamp - oldDetectionTimestamp, 1000)
        }
    }

    fun frameAchieved() {
        allFrames++
        lastFrameAchievedTimestamp = TrueTime.now().time
    }

    fun framePerformed() {
        performedFrames++
        lastFramePerformedTimestamp = TrueTime.now().time
    }

    fun hitRegistered() {
        lastHitTimestamp = TrueTime.now().time
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

        stats.activeDetection = detection

        lastFlushTimestamp = System.currentTimeMillis()
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