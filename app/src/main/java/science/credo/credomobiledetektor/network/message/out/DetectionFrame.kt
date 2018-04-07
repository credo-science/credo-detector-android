package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

/**
 * Created by poznan on 12/09/2017.
 */



class DetectionFrame (hit: Hit,
                      userData: UserInfo.UserData,
                      deviceInfo: IdentityInfo.IdentityData) : OutFrame("detection"){

    class Body (hit: Hit, userData: UserInfo.UserData, deviceInfo: IdentityInfo.IdentityData){
        val detection = hit
        val user_info = userData
        val device_info = deviceInfo
    }

//    val header = FrameOutHeader("detection")
    val body = Body(hit, userData, deviceInfo)
}