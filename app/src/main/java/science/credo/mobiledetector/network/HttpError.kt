package science.credo.mobiledetector.network

import com.google.gson.Gson

data class HttpError(
    val code: Int,
    val cause: String
) {
    fun <T> getCastedCause(clazz: Class<T>): T {
        return Gson().fromJson(cause, clazz)
    }
}