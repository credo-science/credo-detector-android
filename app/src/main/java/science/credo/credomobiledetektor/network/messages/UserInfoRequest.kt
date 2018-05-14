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
data class UserInfoRequest  (
        val display_name: String,
        val team: String,
        val language: String,
        override val device_id: String,
        override val device_type: String,
        override val device_model: String,
        override val system_version: String,
        override val app_version: String
) : BaseDeviceInfoRequest() {
    companion object {
        fun build(display_name: String,
                  team: String,
                  language: String,
                  info: IdentityInfo.IdentityData
        ) : UserInfoRequest {
            return UserInfoRequest(
                    display_name,
                    team,
                    language,
                    info.device_id,
                    info.device_type,
                    info.device_model,
                    info.system_version,
                    info.app_version
            )
        }
    }
}
