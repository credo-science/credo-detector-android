package science.credo.mobiledetector.network

import science.credo.mobiledetector.detector.Hit
import science.credo.mobiledetector.login.IdentityInfo
import java.util.*

class DetectionRequestBody (
    override val device_id: String = "",
    override val device_type: String = "",
    override val device_model: String = "",
    override val system_version: String = "",
    override val app_version: String = "",
    val detections: List<Hit> = LinkedList()
) : BaseDeviceInfoRequest() {
    companion object {
        fun build(info: IdentityInfo.IdentityData, detections: List<Hit>): DetectionRequestBody {
            return DetectionRequestBody(
                info.device_id,
                info.device_type,
                info.device_model,
                info.system_version,
                info.app_version,
                detections
            )
        }
    }
}