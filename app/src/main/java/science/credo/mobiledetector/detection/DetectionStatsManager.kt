package science.credo.mobiledetector.detection

import android.content.Context
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.database.DetectionStateWrapper
import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.network.ServerInterface
import science.credo.mobiledetector.network.messages.PingRequest
import science.credo.mobiledetector.network.messages.build

class DetectionStatsManager {
    private val statsForScreen = DetectionStats()
    private val statsForServer = DetectionStats()

    private var width = 0
    private var height = 0
    private var startDetectionTimestamp = System.currentTimeMillis()

    @Synchronized
    fun updateStats(max: Long, average: Double, zeroes: Double) {
        statsForScreen.updateStats(max, average, zeroes)
        statsForServer.updateStats(max, average, zeroes)
    }

    @Synchronized
    fun frameAchieved(width : Int, height : Int) {
        this.width = width
        this.height = height
        statsForScreen.frameAchieved()
        statsForServer.frameAchieved()
    }

    @Synchronized
    fun framePerformed() {
        statsForScreen.framePerformed()
        statsForServer.framePerformed()
    }

    @Synchronized
    fun hitRegistered() {
        statsForScreen.hitRegistered()
        statsForServer.hitRegistered()
    }

    @Synchronized
    fun flush(context: Context, force : Boolean) {
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

            val deviceInfo : IdentityInfo.IdentityData = IdentityInfo.getDefault(context).getIdentityData()
            doAsync {
                ServerInterface.getDefault(context).ping(build(System.currentTimeMillis(), deviceInfo, statsEvent))
            }

            DetectionStateWrapper.getLatestSession(context).merge(statsEvent)
            DetectionStateWrapper.getTotal(context).merge(statsEvent)
        }
    }

    companion object {
        fun checkNextTimePeriod(last : Long, period : Long) : Boolean {
            return last + period < System.currentTimeMillis()
        }
    }
}
