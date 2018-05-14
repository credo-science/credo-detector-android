package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Base login class, contains fields required by all login methods.
 *
 * @property password
 */
abstract class BaseLoginRequest {
    abstract val password: String
}
