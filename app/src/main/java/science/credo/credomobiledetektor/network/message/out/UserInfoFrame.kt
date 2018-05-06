package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

class UserInfoFrame (userData: UserInfo.UserDataInfo, deviceInfo: IdentityInfo.IdentityData) : OutFrame(deviceInfo) {
    val display_name: String    = userData.displayName
    val team: String            = userData.team
    val language: String        = userData.language
}