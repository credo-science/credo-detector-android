package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

class LoginFrame (userData: UserInfo.UserDataLogin, deviceInfo: IdentityInfo.IdentityData) : OutFrame(deviceInfo) {
    val email: String       = userData.email
    val password: String    = userData.password
}