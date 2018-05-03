package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

class LoginFrame (key: String, deviceInfo: IdentityInfo.IdentityData) : OutFrame(deviceInfo) {
    val email: String = "email@example.com"
    val username: String = "username"
    val password: String = "password"
}