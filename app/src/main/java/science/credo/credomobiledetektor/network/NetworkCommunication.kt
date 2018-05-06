package science.credo.credomobiledetektor.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
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

    /**
     * Sends GET request.
     *
     * @param endpoint Endpoint that receives the request.
     * @return Response object
     */
    fun get(endpoint : String): Response {
        try {
            val request = Request.Builder()
                    .url(mServiceUrl + endpoint)
                    .build();

            val response = client.newCall(request).execute();
            val responseString = response.body()?.string() ?: ""
            return Response(response.code(), responseString)
        } catch (e: IOException) {
            return Response(0, "")
        }
    }

    /**
     * Sends POST request.
     *
     * @param endpoint Endpoint that receives the request.
     * @param json JSON string sent in request body.
     * @return Response object
     */
    fun post(endpoint: String, json: String): Response {
        try {
            Log.d(TAG, "post: (${json.length}) $json")

            val body = RequestBody.create(JSON, json)
            //Log.d("BODY",body.contentType())
            val request = Request.Builder()
                    .url(mServiceUrl + endpoint)
                    .post(body)
                    .build()
            val response = client.newCall(request).execute()
            val responseString = response.body()?.string() ?: ""
            Log.d(TAG, "post result: ${response.code()} $responseString")
            return Response(response.code(), responseString)
        } catch (e: IOException) {
            Log.d("Post", e.toString())
            return Response(0, "")
        }
    }
}