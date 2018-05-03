package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

class UserInfoFrame (userData: UserInfo.UserData, deviceInfo: IdentityInfo.IdentityData) : OutFrame(deviceInfo) {

    class Body (userData: UserInfo.UserData, deviceInfo: IdentityInfo.IdentityData){
        val user_info = userData
    }

    val body = Body(userData, deviceInfo)
}