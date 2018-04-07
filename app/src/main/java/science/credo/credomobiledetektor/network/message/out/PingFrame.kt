package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

/**
 * Created by poznan on 12/09/2017.
 */

class PingFrame (userData: UserInfo.UserData,
                 deviceInfo: IdentityInfo.IdentityData)
    : OutFrame("ping") {
    class Body (userData: UserInfo.UserData, deviceInfo: IdentityInfo.IdentityData) {
        val user_info = userData
        val device_info = deviceInfo
    }
//    val header = FrameOutHeader("ping")
    val body = Body(userData, deviceInfo);
}