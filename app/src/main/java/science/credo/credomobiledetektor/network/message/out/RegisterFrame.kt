package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

/**
 * Created by poznan on 19/09/2017.
 */

class RegisterFrame (userData: UserInfo.UserDataRegister,
                 deviceInfo: IdentityInfo.IdentityData)
    : OutFrame("register") {
//    class RegisterInfo (_user_name: String, _email: String, passWord: String, team: String)
    class Body (userData: UserInfo.UserDataRegister, deviceInfo: IdentityInfo.IdentityData) {
        val user_info = userData
        val device_info = deviceInfo
    }
    //    val header = FrameOutHeader("ping")
    val body = Body(userData, deviceInfo);
}