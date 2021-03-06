package science.credo.mobiledetector2.login

data class LoginByUsernameRequestBody(
    val username: String,
    override val password: String,
    override val device_id: String,
    override val device_type: String,
    override val device_model: String,
    override val system_version: String,
    override val app_version: String
) : BaseLoginRequestBody() {
    companion object {
        fun build(username: String,
                  password: String,
                  info: IdentityInfo.IdentityData
        ) : LoginByUsernameRequestBody {
            return LoginByUsernameRequestBody(
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