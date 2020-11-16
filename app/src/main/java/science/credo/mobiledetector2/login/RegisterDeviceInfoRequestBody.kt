package science.credo.mobiledetector2.login

import science.credo.mobiledetector2.network.BaseDeviceInfoRequest

data class RegisterDeviceInfoRequestBody  (
    val email: String,
    val username: String,
    val display_name: String,
    val password: String,
    val team: String,
    val language: String,
    override val device_id: String,
    override val device_type: String,
    override val device_model: String,
    override val system_version: String,
    override val app_version: String
) : BaseDeviceInfoRequest() {
    companion object {
        fun build(email: String,
                  username: String,
                  display_name: String,
                  password: String,
                  team: String,
                  language: String,
                  info: IdentityInfo.IdentityData
        ) : RegisterDeviceInfoRequestBody {
            return RegisterDeviceInfoRequestBody(
                email,
                username,
                display_name,
                password,
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