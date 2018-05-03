package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

/**
 * Created by poznan on 12/09/2017.
 */

class PingFrame (deviceInfo: IdentityInfo.IdentityData) : OutFrame(deviceInfo) {
    val delta_time: Int = 0
    val timestamp: Int = 0
}