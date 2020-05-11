package science.credo.mobiledetector.login

data class LoginByCodeRequestBody(
    val provider: String,
    override val authorization_code: String,
    override val device_id: String,
    override val device_type: String,
    override val device_model: String,
    override val system_version: String,
    override val app_version: String
) : BaseLoginCodeRequestBody() {
    companion object {
        fun build(provider: String,
                  authorization_code: String,
                  info: IdentityInfo.IdentityData
        ) : LoginByCodeRequestBody {
            return LoginByCodeRequestBody(
                provider,
                authorization_code,
                info.device_id,
                info.device_type,
                info.device_model,
                info.system_version,
                info.app_version
            )
        }
    }
}