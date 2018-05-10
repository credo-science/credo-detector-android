package science.credo.credomobiledetektor.detection

import org.greenrobot.eventbus.EventBus
import science.credo.credomobiledetektor.events.StatsEvent
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.messages.PingRequest

class DetectionStatsManager {
    private val statsForScreen = DetectionStats()
    private val statsForServer = DetectionStats()

    private var width = 0
    private var height = 0
    private var startDetectionTimestamp = System.currentTimeMillis()

    fun updateStats(max: Long, average: Double, zeroes: Long) {
        statsForScreen.updateStats(max, average, zeroes)
        statsForServer.updateStats(max, average, zeroes)
    }

    fun frameAchieved(width : Int, height : Int) {
        this.width = width
        this.height = height
        statsForScreen.frameAchieved()
        statsForServer.frameAchieved()
    }

    fun framePerformed() {
        statsForScreen.framePerformed()
        statsForServer.framePerformed()
    }

    fun hitRegistered() {
        statsForScreen.hitRegistered()
        statsForServer.hitRegistered()
    }

    fun flush(force : Boolean) {
        val screenCondition = checkNextTimePeriod(statsForScreen.lastFlushTimestamp, 1000L)
        val serverCondition = checkNextTimePeriod(statsForServer.lastFlushTimestamp, 600000L)

        if (force || screenCondition) {
            val statsEvent = StatsEvent(width, height, startDetectionTimestamp)
            statsForScreen.flush(statsEvent, false)
            EventBus.getDefault().post(statsEvent)
        }

        if (force || serverCondition) {
            val statsEvent = StatsEvent(width, height, startDetectionTimestamp)
            statsForServer.flush(statsEvent,  true)
            TODO("send ping request")
//            val deviceInfo : IdentityInfo.IdentityData = IdentityInfo.getInstance(context).getIdentityData()
//            ServerInterface.getDefault().ping(PingRequest(statsEvent.lastHitTimestamp, System.currentTimeMillis(), deviceInfo))
        }
    }

    companion object {
        fun checkNextTimePeriod(last : Long, period : Long) : Boolean {
            return last + period < System.currentTimeMillis()
        }
    }
}
