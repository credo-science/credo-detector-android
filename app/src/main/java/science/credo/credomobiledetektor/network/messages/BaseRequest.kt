package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Base class. It contains fields that are required in every request.
 *
 * @property device_id
 * @property device_type
 * @property device_model
 * @property system_version
 * @property app_version
 */
abstract class BaseRequest(deviceInfo: IdentityInfo.IdentityData) {
    val device_id: String       = deviceInfo.device_id
    val device_type: String     = deviceInfo.device_type
    val device_model: String    = deviceInfo.device_model
    val system_version: String  = deviceInfo.system_version
    val app_version: String     = deviceInfo.app_version
}