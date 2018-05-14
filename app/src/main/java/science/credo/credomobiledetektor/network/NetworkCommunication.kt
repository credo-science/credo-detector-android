package science.credo.credomobiledetektor.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import science.credo.credomobiledetektor.database.ConfigurationWrapper
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * Core of networking logic. Handles GET and POST requests.
 *
 * @property TAG Tag used in logging.
 * @property mServiceUrl API base URL.
 * @property client HTTP client which executes all requests.
 * @property JSON MediaType object, determines content type.
 */
class NetworkCommunication(val context: Context) {
    private val cw = ConfigurationWrapper(context)
    data class Response(val code: Int, val message: String)

    private fun prepareRequest(endpoint : String, token: String? = "") : Request.Builder {
        val serverUrl = cw.endpoint.removeSuffix("/")
        val builder = Request.Builder().url(serverUrl + endpoint)
        Log.v(TAG, "Use endpoint prefix: $serverUrl")

        if (token != "") builder.header("Authorization", "Token $token")

        return builder
    }

    private fun prepareResponse(request: Request): Response {
        return try {
            val response = client.newCall(request).execute()
            val responseString = response.body()?.string() ?: ""
            Log.v(TAG, "Response: ${response.code()}\n\n$responseString")
            Response(response.code(), responseString)
        } catch (e: IOException) {
            Log.w(TAG, "Communication error", e)
            Response(0, "")
        }
    }

    /**
     * Sends GET request.
     *
     * @param endpoint Endpoint that receives the request.
     * @return Response object
     */
    fun get(endpoint: String, token: String? = ""): Response {
        Log.v(TAG, "GET $endpoint")
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
        Log.v(TAG, "POST $endpoint\n\n$json")
        return prepareResponse(
            prepareRequest(endpoint, token).post(RequestBody.create(JSON, json)).build()
        )
    }

    companion object {
        private val TAG = "NetworkCommunication"
        private val JSON = MediaType.parse("application/json; charset=utf-8")

        private val trustManager = object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

        private fun getSSLSocketFactory(): SSLSocketFactory? {
            return try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(trustManager)

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                // Create an ssl socket factory with our all-trusting manager

                sslContext.socketFactory
            } catch (e: KeyManagementException) {
                null
            } catch (e: NoSuchAlgorithmException) {
                null
            }
        }

        private val client: OkHttpClient = OkHttpClient
                .Builder()
                .sslSocketFactory(getSSLSocketFactory()!!, trustManager)
                .build()
    }
}