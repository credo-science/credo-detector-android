package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Login using username.
 *
 * @property username
 * @property password
 */
data class LoginByUsernameRequest(
        val username: String,
        override val password: String
) : BaseLoginRequest()
