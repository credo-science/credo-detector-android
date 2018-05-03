package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

class RegisterFrame (userData: UserInfo.UserDataRegister, deviceInfo: IdentityInfo.IdentityData) : OutFrame(deviceInfo) {
    val email: String           = userData.email
    val username: String        = userData.username
    val display_name: String    = userData.displayName
    val password: String        = userData.password
    val team: String            = userData.team
    // ISO 639-1 language code representation
    val language: String        = userData.language
}