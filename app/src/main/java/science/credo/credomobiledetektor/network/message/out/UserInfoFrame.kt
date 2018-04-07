package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

/**
 * Created by poznan on 12/09/2017.
 */

class UserInfoFrame (userData: UserInfo.UserData,
                     deviceInfo: IdentityInfo.IdentityData)
    : OutFrame("user_data") {

    class Body (userData: UserInfo.UserData, deviceInfo: IdentityInfo.IdentityData){
        val user_info = userData
        val device_info = deviceInfo
    }

    val body = Body(userData, deviceInfo)
}