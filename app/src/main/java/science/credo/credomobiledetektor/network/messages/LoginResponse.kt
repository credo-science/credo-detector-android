package science.credo.credomobiledetektor.network.messages

class LoginResponse (
        val message: String?,
        val username: String,
        val display_name: String?,
        val email: String,
        val team: String,
        // ISO 639-1 language code representation
        val language: String,

        val token: String
)