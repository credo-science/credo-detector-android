package science.credo.mobiledetector.login

/**
 * Response object created after successful login.
 *
 * @property message Response message.
 * @property username
 * @property display_name User display name.
 * @property email User email address.
 * @property team Team that user belongs to.
 * @property language ISO 639-1 language code representation.
 * @property token Authentication token. It's used in request header to authorize user.
 */
data class LoginResponse (
    val message: String?,
    val username: String,
    val display_name: String,
    val email: String,
    val team: String,
    val language: String,
    val token: String
)