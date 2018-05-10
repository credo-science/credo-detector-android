package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Login using email address.
 *
 * @property email
 * @property password
 */
class LoginByEmailRequest(
        val email: String,
        password: String,
        deviceInfo: IdentityInfo.IdentityData
) : BaseLoginRequest (password, deviceInfo)
