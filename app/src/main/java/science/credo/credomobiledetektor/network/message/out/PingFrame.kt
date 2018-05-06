package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.events.StatsEvent
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

/**
 * Created by poznan on 12/09/2017.
 */

class PingFrame (deviceInfo: IdentityInfo.IdentityData, stats: StatsEvent) : OutFrame(deviceInfo) {
    val delta_time: Long = stats.lastHitTimestamp
    val timestamp: Long  = System.currentTimeMillis()
}