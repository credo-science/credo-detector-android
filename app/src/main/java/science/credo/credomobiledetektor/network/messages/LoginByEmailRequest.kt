package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Login using email address.
 *
 * @property email
 * @property password
 */
data class LoginByEmailRequest(
        val email: String,
        override val password: String
) : BaseLoginRequest()
