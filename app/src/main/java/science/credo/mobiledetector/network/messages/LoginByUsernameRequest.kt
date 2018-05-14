package science.credo.mobiledetector.network.messages

import science.credo.mobiledetector.info.IdentityInfo

/**
 * Login using username.
 *
 * @property username
 * @property password
 */
data class LoginByUsernameRequest(
        val username: String,
        override val password: String,
        override val device_id: String,
        override val device_type: String,
        override val device_model: String,
        override val system_version: String,
        override val app_version: String
) : BaseLoginRequest() {
    companion object {
        fun build(username: String,
                  password: String,
                  info: IdentityInfo.IdentityData
        ) : LoginByUsernameRequest {
            return LoginByUsernameRequest(
                    username,
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
