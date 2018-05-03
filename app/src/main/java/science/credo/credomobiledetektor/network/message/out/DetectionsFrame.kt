package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo

class DetectionsFrame (hits: MutableList<Hit>, deviceInfo: IdentityInfo.IdentityData) : OutFrame(deviceInfo){
    val detections = hits
}