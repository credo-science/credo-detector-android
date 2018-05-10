package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Send detected particles to the server.
 *
 * @property detections List of submitted detections.
 */
class DetectionRequest(
    val detections: MutableList<Hit>,
    deviceInfo: IdentityInfo.IdentityData
) : BaseRequest(deviceInfo)
