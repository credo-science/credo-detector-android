package science.credo.credomobiledetektor.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import science.credo.credomobiledetektor.database.UserInfoWrapper
import java.io.IOException

/**
 * Core of networking logic. Handles GET and POST requests.
 *
 * @property TAG Tag used in logging.
 * @property mServiceUrl API base URL.
 * @property client HTTP client which executes all requests.
 * @property JSON MediaType object, determines content type.
 */
object NetworkCommunication {
    val TAG = "NetworkCommunication"
//    val mServiceUrl = "https://api.credo.science/"
    val mServiceUrl = "http://145.239.92.63:8000/api/v2"
    val client: OkHttpClient = OkHttpClient()
    val JSON = MediaType.parse("application/json; charset=utf-8")

    data class Response(val code: Int, val message: String) {}

    private fun prepareRequest(endpoint : String, token: String? = "") : Request.Builder {
        val builder = Request.Builder().url(mServiceUrl + endpoint)

        if(token != "") builder.header("Authorization", "Token $token")

        return builder
    }

    private fun prepareResponse(request: Request) : Response {
        return try {
            val response = client.newCall(request).execute()
            val responseString = response.body()?.string() ?: ""
            Response(response.code(), responseString)
        } catch (e: IOException) {
            Response(0, "")
        }
    }

    /**
     * Sends GET request.
     *
     * @param endpoint Endpoint that receives the request.
     * @return Response object
     */
    fun get(endpoint : String, token: String? = ""): Response {
        return prepareResponse(prepareRequest(endpoint, token).build())
    }

    /**
     * Sends POST request.
     *
     * @param endpoint Endpoint that receives the request.
     * @param json JSON string sent in request body.
     * @return Response object
     */
    fun post(endpoint: String, json: String, token: String? = ""): Response {
        return prepareResponse(
                prepareRequest(endpoint, token).post(RequestBody.create(JSON, json)).build()
        )
    }
}