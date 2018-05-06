package science.credo.credomobiledetektor.network.messages

class RegisterRequest (
        val email: String,
        val username: String,
        val display_name: String,
        val password: String,
        val team: String,
        // ISO 639-1 language code representation
        val language: String,

        val device_id: String,
        val device_type: String,
        val device_model: String,
        val system_version: String,
        val app_version: String
)
