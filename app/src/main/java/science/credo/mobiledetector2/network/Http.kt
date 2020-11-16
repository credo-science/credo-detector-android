package science.credo.mobiledetector2.network

import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


object Http {


    suspend fun sendGetRequest(
        url: String
    ): Response {
        return sendGetRequest(url, null)
    }

    suspend fun sendGetRequest(
        url: String,
        vararg params: Parameter
    ): Response {
        return sendGetRequest(url, null, *params)
    }


    suspend fun sendGetRequest(
        url: String,
        accessToken: String
    ): Response {
        return sendGetRequest(url, Pair("X-Access-Token", accessToken))
    }

    suspend fun sendGetRequest(
        url: String,
        accessToken: String,
        vararg params: Parameter
    ): Response {
        return sendGetRequest(url, Pair("X-Access-Token", accessToken), *params)
    }

    suspend fun sendGetRequest(
        url: String,
        accessToken: Pair<String, String>?,
        vararg params: Parameter
    ): Response {
        var urlWithParams = url
        if (params.isNotEmpty()) {
            urlWithParams += "%${params[0].name}=${params[0].value}"
        }
        for (i in 1 until params.size) {
            urlWithParams += "&${params[i].name}=${params[i].value}"
        }
        return sendGetRequest(urlWithParams, accessToken)
    }

    suspend fun sendGetRequest(
        url: String,
        accessToken: Pair<String, String>?
    ): Response {
        return GlobalScope.async {
            try {
                val uc = URL(url).openConnection() as HttpURLConnection
                uc.readTimeout = 10000
                uc.connectTimeout = 15000
                uc.requestMethod = "GET"

                if (accessToken != null) {
                    uc.setRequestProperty(accessToken.first, accessToken.second)
                }


                uc.connect()
                val responseStringBuffer = StringBuffer()
                val br: BufferedReader = if (uc.responseCode != 200) {
                    BufferedReader(InputStreamReader(uc.errorStream))
                } else {
                    BufferedReader(InputStreamReader(uc.inputStream))
                }
                var line = br.readLine()
                while (line != null) {
                    responseStringBuffer.append(line)
                    line = br.readLine()
                }
                br.close()
                val response = responseStringBuffer.toString()
                return@async Response(uc.responseCode, response)
            } catch (e: IOException) {
                e.printStackTrace()
                return@async Response(-1, e.message ?: "IOException null message")
            }
        }.await()
    }


    suspend fun sendPostRequestFormData(
        url: String,
        vararg params: Parameter
    ): Response {
        return sendPostRequestFormData(url, null, *params)
    }

    suspend fun sendPostRequestFormData(
        url: String,
        accessToken: String,
        vararg params: Parameter
    ): Response {
        return sendPostRequestFormData(url, Pair("X-Access-Token", accessToken), *params)
    }

    suspend fun sendPostRequestFormData(
        url: String,
        accessToken: Pair<String, String>?,
        vararg params: Parameter
    ): Response {

        return GlobalScope.async {
            try {
                val uc = URL(url).openConnection() as HttpURLConnection
                uc.readTimeout = 10000
                uc.connectTimeout = 15000
                uc.requestMethod = "POST"
                uc.doInput = true
                uc.doOutput = true
                if (accessToken != null) {
                    uc.setRequestProperty(accessToken.first, accessToken.second)
                }
                val os = uc.outputStream
                val writer = OutputStreamWriter(os, "UTF-8")
                if (params.isNotEmpty()) {
                    writer.append("${params[0].name}=${params[0].value}")
                }
                for (i in 1 until params.size) {
                    writer.append("&${params[i].name}=${params[i].value}")
                }

                writer.flush()
                writer.close()
                os.close()
                uc.connect()
                val responseStringBuffer = StringBuffer()
                val br: BufferedReader = if (uc.responseCode != 200) {
                    BufferedReader(InputStreamReader(uc.errorStream))
                } else {
                    BufferedReader(InputStreamReader(uc.inputStream))
                }
                var line = br.readLine()
                while (line != null) {
                    responseStringBuffer.append(line)
                    line = br.readLine()
                }
                br.close()
                val response = responseStringBuffer.toString()
                return@async Response(uc.responseCode, response)
            } catch (e: IOException) {
                e.printStackTrace()
                return@async Response(-1, e.message ?: "IOException null message")
            }
        }.await()
    }


    suspend fun sendPostRequest(
        url: String
    ): Response {
        return sendPostRequestRaw(url, null, null)
    }

    suspend fun sendPostRequest(
        url: String,
        accessToken: Pair<String, String>?
    ): Response {
        return sendPostRequestRaw(url, accessToken, null)
    }


    suspend fun sendPostRequest(
        url: String,
        accessToken: String
    ): Response {
        return sendPostRequestRaw(url, Pair("X-Access-Token", accessToken), null)
    }

    suspend fun sendPostRequestRaw(
        url: String,
        params: Any?
    ): Response {
        return sendPostRequestRaw(url, null, params)
    }

    suspend fun sendPostRequestRaw(
        url: String,
        accessToken: String,
        params: Any?
    ): Response {
        return sendPostRequestRaw(url, Pair("X-Access-Token", accessToken), params)
    }

    suspend fun sendPostRequestRaw(
        url: String,
        accessToken: Pair<String, String>?,
        params: Any?
    ): Response {

        return GlobalScope.async {
            val uc = URL(url).openConnection() as HttpURLConnection
            try {
                uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                uc.requestMethod = "POST"
                uc.readTimeout = 10000
                uc.connectTimeout = 15000
                uc.doInput = true
                uc.doOutput = true
                if (accessToken != null) {
                    uc.setRequestProperty(accessToken.first, accessToken.second)
                }
                uc.instanceFollowRedirects = false
                if (params != null) {
                    val writer = OutputStreamWriter(uc.outputStream, "UTF-8")
                    writer.write(Gson().toJson(params))
                    writer.close()
                }
                uc.connect()

                val responseStringBuffer = StringBuffer()
                val br: BufferedReader = if (uc.responseCode != 200) {
                    BufferedReader(InputStreamReader(uc.errorStream))
                } else {
                    BufferedReader(InputStreamReader(uc.inputStream))
                }

                var line = br.readLine()
                while (line != null) {
                    responseStringBuffer.append(line)
                    line = br.readLine()
                }
                br.close()
                val response = responseStringBuffer.toString()
                return@async Response(uc.responseCode, response)
            } catch (e: IOException) {
                return@async Response(-2, e.message ?: "null")
            }
        }.await()
    }


    suspend fun sendPutRequestRaw(
        url: String,
        params: Any?
    ): Response {
        return sendPutRequestRaw(url, null, params)
    }


    suspend fun sendPutRequestRaw(
        url: String,
        accessToken: String,
        params: Any?
    ): Response {
        return sendPutRequestRaw(url, Pair("X-Access-Token", accessToken), params)
    }

    suspend fun sendPutRequestRaw(
        url: String,
        accessToken: Pair<String, String>?,
        params: Any?
    ): Response {

        return GlobalScope.async {
            val uc = URL(url).openConnection() as HttpURLConnection
            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            uc.requestMethod = "PUT"
            uc.readTimeout = 10000
            uc.connectTimeout = 15000
            uc.doInput = true
            uc.doOutput = true
            if (accessToken != null) {
                uc.setRequestProperty(accessToken.first, accessToken.second)
            }
            uc.instanceFollowRedirects = false
            if (params != null) {
                val writer = OutputStreamWriter(uc.outputStream, "UTF-8")
                writer.write(Gson().toJson(params))
                writer.close()
            }
            uc.connect()

            val responseStringBuffer = StringBuffer()
            val br: BufferedReader = if (uc.responseCode != 200) {
                BufferedReader(InputStreamReader(uc.errorStream))
            } else {
                BufferedReader(InputStreamReader(uc.inputStream))
            }

            var line = br.readLine()
            while (line != null) {
                responseStringBuffer.append(line)
                line = br.readLine()
            }
            br.close()
            val response = responseStringBuffer.toString()
            return@async Response(uc.responseCode, response)
        }.await()
    }


    suspend fun sendPatchRequestRaw(
        url: String,
        params: Any?
    ): Response {
        return sendPatchRequestRaw(url, null, params)
    }


    suspend fun sendPatchRequestRaw(
        url: String,
        accessToken: String,
        params: Any?
    ): Response {
        return sendPatchRequestRaw(url, Pair("X-Access-Token", accessToken), params)
    }

    suspend fun sendPatchRequestRaw(
        url: String,
        accessToken: Pair<String, String>?,
        params: Any?
    ): Response {

        return GlobalScope.async {
            val uc = URL(url).openConnection() as HttpURLConnection
            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            uc.requestMethod = "PATCH"
            uc.readTimeout = 10000
            uc.connectTimeout = 15000
            uc.doInput = true
            uc.doOutput = true
            if (accessToken != null) {
                uc.setRequestProperty(accessToken.first, accessToken.second)
            }
            uc.instanceFollowRedirects = false
            if (params != null) {
                val writer = OutputStreamWriter(uc.outputStream, "UTF-8")
                writer.write(Gson().toJson(params))
                writer.close()
            }
            uc.connect()

            val responseStringBuffer = StringBuffer()
            val br: BufferedReader = if (uc.responseCode != 200) {
                BufferedReader(InputStreamReader(uc.errorStream))
            } else {
                BufferedReader(InputStreamReader(uc.inputStream))
            }

            var line = br.readLine()
            while (line != null) {
                responseStringBuffer.append(line)
                line = br.readLine()
            }
            br.close()
            val response = responseStringBuffer.toString()
            return@async Response(uc.responseCode, response)
        }.await()
    }

    suspend fun sendPatchRequestFormData(
        url: String,
        vararg params: Parameter
    ): Response {
        return sendPatchRequestFormData(url, null, *params)
    }

    suspend fun sendPatchRequestFormData(
        url: String,
        accessToken: String,
        vararg params: Parameter
    ): Response {
        return sendPatchRequestFormData(url, Pair("X-Access-Token", accessToken), *params)
    }

    suspend fun sendPatchRequestFormData(
        url: String,
        accessToken: Pair<String, String>?,
        vararg params: Parameter
    ): Response {

        return GlobalScope.async {
            val uc = URL(url).openConnection() as HttpURLConnection
            uc.readTimeout = 10000
            uc.connectTimeout = 15000
            uc.requestMethod = "PATCH"
            uc.doInput = true
            uc.doOutput = true
            if (accessToken != null) {
                uc.setRequestProperty(accessToken.first, accessToken.second)
            }
            val os = uc.outputStream
            val writer = OutputStreamWriter(os, "UTF-8")
            if (params.isNotEmpty()) {
                writer.append("${params[0].name}=${params[0].value}")
            }
            for (i in 1 until params.size) {
                writer.append("&${params[i].name}=${params[i].value}")
            }

            writer.flush()
            writer.close()
            os.close()
            uc.connect()
            val responseStringBuffer = StringBuffer()
            val br: BufferedReader = if (uc.responseCode != 200) {
                BufferedReader(InputStreamReader(uc.errorStream))
            } else {
                BufferedReader(InputStreamReader(uc.inputStream))
            }
            var line = br.readLine()
            while (line != null) {
                responseStringBuffer.append(line)
                line = br.readLine()
            }
            br.close()
            val response = responseStringBuffer.toString()
            return@async Response(uc.responseCode, response)
        }.await()
    }

}