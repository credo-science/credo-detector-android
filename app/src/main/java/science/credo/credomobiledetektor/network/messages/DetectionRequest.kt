package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import java.util.*

/**
 * Send detected particles to the server.
 *
 * @property detections List of submitted detections.
 */
data class DetectionRequest(
        override val device_id: String = "",
        override val device_type: String = "",
        override val device_model: String = "",
        override val system_version: String = "",
        override val app_version: String = "",
        val detections: MutableList<Hit> = LinkedList()
) : BaseDeviceInfoRequest() {
    companion object {
        fun build(info: IdentityInfo.IdentityData, detections: MutableList<Hit>): DetectionRequest {
            return DetectionRequest(
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
