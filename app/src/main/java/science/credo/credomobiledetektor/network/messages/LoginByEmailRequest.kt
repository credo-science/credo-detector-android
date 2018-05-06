package science.credo.credomobiledetektor.network.messages

class LoginByEmailRequest(
        val email: String,
        password: String,
        device_id: String,
        device_type: String,
        device_model: String,
        system_version: String,
        app_version: String
) : BaseLoginRequest (password, device_id, device_type, device_model, system_version, app_version)
