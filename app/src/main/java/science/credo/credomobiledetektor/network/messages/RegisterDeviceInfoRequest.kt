package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Contains registration fields used to create new user account.
 *
 * @property email Email address, must be unique.
 * @property username
 * @property display_name User display name.
 * @property password
 * @property team Team that user wants to join.
 * @property language ISO 639-1 language code representation.
 */
data class RegisterDeviceInfoRequest (
        val email: String,
        val username: String,
        val display_name: String,
        val password: String,
        val team: String,
        val language: String
)
