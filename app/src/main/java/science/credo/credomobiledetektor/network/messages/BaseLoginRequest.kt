package science.credo.credomobiledetektor.network.messages

/**
 * Base login class, contains fields required by all login methods.
 *
 * @property password
 */
abstract class BaseLoginRequest (
        val password: String,

        val device_id: String,
        val device_type: String,
        val device_model: String,
        val system_version: String,
        val app_version: String
)
