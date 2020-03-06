package science.credo.mobiledetector.login

data class LoginByEmailRequestBody(
    val email: String,
    override val password: String,
    override val device_id: String,
    override val device_type: String,
    override val device_model: String,
    override val system_version: String,
    override val app_version: String
) : BaseLoginRequestBody() {
    companion object {
        fun build(email: String,
                  password: String,
                  info: IdentityInfo.IdentityData
        ) : LoginByEmailRequestBody {
            return LoginByEmailRequestBody(
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