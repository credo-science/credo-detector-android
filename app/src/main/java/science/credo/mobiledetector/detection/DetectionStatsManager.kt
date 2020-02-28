package science.credo.mobiledetector.detection

import android.content.Context
import com.instacart.library.truetime.TrueTimeRx
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.database.DetectionStateWrapper
import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.network.ServerInterface
import science.credo.mobiledetector.network.messages.PingRequest
import science.credo.mobiledetector.network.messages.build

class DetectionStatsManager {
    private val statsForScreen = DetectionStats() // cleans after start detections, cleans pixel brights stats only
    private val statsForServer = DetectionStats() // cleans after ping
    private val statsForTotal = DetectionStats() // never cleans

    private var width = 0
    private var height = 0
    private var startDetectionTimestamp = TrueTimeRx.now().time

    /**
     * Call for update stats for pixels brights.
     */
    @Synchronized
    fun updateStats(max: Int, average: Double, zeroes: Double) {
        statsForScreen.updateStats(max, average, zeroes)
        //statsForServer and statsForTotal is not updated because it will be updated in  framePerformed
    }

    @Synchronized
    fun activeDetect(detectionOn:Boolean) {
        statsForScreen.activeDetect(detectionOn)
        statsForServer.activeDetect(detectionOn)
        statsForTotal.activeDetect(detectionOn)
    }

    /**
     * Call for update count of all frames.
     */
    @Synchronized
    fun frameAchieved(width : Int, height : Int) {
        this.width = width
        this.height = height
        statsForScreen.frameAchieved()
        statsForServer.frameAchieved()
        statsForTotal.frameAchieved()
    }

    /**
     * Call for update count and pixels brights stats for
     */
    @Synchronized
    fun framePerformed(max: Int, average: Double, zeroes: Double) {
        statsForScreen.framePerformed()
        statsForServer.framePerformed()
        statsForTotal.framePerformed()

        //statsForScreen - not updated because is just updated in updateStats
        statsForServer.updateStats(max, average, zeroes)
        statsForTotal.updateStats(max, average, zeroes)

        statsForScreen.activeDetect(true)
        statsForServer.activeDetect(true)
        statsForTotal.activeDetect(true)
    }

    @Synchronized
    fun hitRegistered() {
        statsForScreen.hitRegistered()
        statsForServer.hitRegistered()
        statsForTotal.hitRegistered()
    }

    /**
     * Publish stats for other modules when its time.
     * @param context - current Android's context
     * @param force - no check time condition before publish
     */
    @Synchronized
    fun flush(context: Context, force : Boolean) {

        // time to publish for screen condition
        val screenCondition = checkNextTimePeriod(statsForScreen.lastFlushTimestamp, 1000L)

        // time to publish for server condition (send Ping message)
        val serverCondition = checkNextTimePeriod(statsForServer.lastFlushTimestamp, 600000L)

        // time to store total stats in phone memory (prevent to damage flash memory) condition
        val totalCondition = checkNextTimePeriod(statsForTotal.lastFlushTimestamp, 60000L)

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
        }

        if (force || totalCondition) {
            val statsEvent = StatsEvent(width, height, startDetectionTimestamp)
            statsForTotal.flush(statsEvent,  true)
            DetectionStateWrapper.getTotal(context).merge(statsEvent)
        }
    }

    companion object {
        fun checkNextTimePeriod(last : Long, period : Long) : Boolean {
            return last + period < TrueTimeRx.now().time
        }
    }
}
