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
        override val password: String,
        override val device_id: String,
        override val device_type: String,
        override val device_model: String,
        override val system_version: String,
        override val app_version: String
) : BaseLoginRequest() {
    companion object {
        fun build(email: String,
                  password: String,
                  info: IdentityInfo.IdentityData
        ) : LoginByEmailRequest {
            return LoginByEmailRequest(
                    email,
                    password,
                    info.device_id,
                    info.device_type,
                    info.device_model,
                    info.system_version,
                    info.app_version
            )
        }
    }
}
