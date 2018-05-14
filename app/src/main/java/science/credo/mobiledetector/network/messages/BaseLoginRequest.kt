package science.credo.mobiledetector.network.messages

import science.credo.mobiledetector.info.IdentityInfo

/**
 * Base login class, contains fields required by all login methods.
 *
 * @property password
 */
abstract class BaseLoginRequest : BaseDeviceInfoRequest() {
    abstract val password: String
}
