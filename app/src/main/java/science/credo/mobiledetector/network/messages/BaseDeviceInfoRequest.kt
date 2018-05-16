package science.credo.mobiledetector.network.messages

import science.credo.mobiledetector.info.IdentityInfo

/**
 * Base class. It contains fields that are required in every request.
 *
 * @property device_id
 * @property device_type
 * @property device_model
 * @property system_version
 * @property app_version
 */
abstract class BaseDeviceInfoRequest {
    abstract val device_id: String
    abstract val device_type: String
    abstract val device_model: String
    abstract val system_version: String
    abstract val app_version: String
}
