package science.credo.credomobiledetektor.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import java.io.IOException


/**
 * Created by poznan on 26/08/2017.
 */

object NetworkCommunication {

    val TAG = "NetworkCommunication"
    val mServiceUrl = "https://api.credo.science/"

    data class Response(val code: Int, val message: String) {}

    val client: OkHttpClient = OkHttpClient()

    fun get(path : String): Response {
        try {
            val request = Request.Builder()
                    .url(mServiceUrl+path)
                    .build();

            val response = client.newCall(request).execute();
            val responseString = response.body()?.string() ?: ""
            return Response(response.code(), responseString)
        } catch (e: IOException) {
            return Response(0, "")
        }
    }

    val JSON = MediaType.parse("application/json; charset=utf-8")

    fun post(json: String): Response {
        try {
            Log.d(TAG, "post: (${json.length}) $json")

            val body = RequestBody.create(JSON, json)
            //Log.d("BODY",body.contentType())
            val request = Request.Builder()
                    .url(mServiceUrl)
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

//TODO zamykanie sokectów