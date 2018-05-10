package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Login using username.
 *
 * @property username
 * @property password
 */
class LoginByUsernameRequest(
        val username: String,
        password: String,
        deviceInfo: IdentityInfo.IdentityData
) : BaseLoginRequest (password, deviceInfo)
