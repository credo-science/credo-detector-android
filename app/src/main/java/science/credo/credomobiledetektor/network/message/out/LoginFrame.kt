package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

/**
 * Created by poznan on 19/09/2017.
 */

class LoginFrame (key: String,
                 deviceInfo: IdentityInfo.IdentityData)
    : OutFrame("login") {
    class KeyInfo (k: String) {
        val key = k
    }
    class Body (key: String, deviceInfo: IdentityInfo.IdentityData) {
        val key_info = KeyInfo(key)
        val device_info = deviceInfo
    }
    //    val header = FrameOutHeader("ping")
    val body = Body(key, deviceInfo);
}