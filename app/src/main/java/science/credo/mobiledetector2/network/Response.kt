package science.credo.mobiledetector2.network

import com.google.gson.Gson


class Response(
    private val code: Int,
    private val response: String
) {
    val gson: Gson = Gson()

    fun isSuccess(): Boolean {
        return code == 200
    }

    suspend fun <T> getCastedResponse(clazz: Class<T>): T? {
        return getCastedResponse(gson, clazz)
    }

    suspend fun <T> getCastedResponse(gson: Gson, clazz: Class<T>): T? {
        return if (isSuccess()) {
            gson.fromJson(response, clazz)
        } else {
            null
        }
    }

    suspend fun getResponse(): String {
        return response
    }

    suspend fun getCode(): Int {
        return code
    }

    suspend fun getError(): HttpError? {
        return if (!isSuccess()) {
            HttpError(code, response)
        } else {
            return null
        }
    }

}