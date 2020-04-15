package science.credo.mobiledetector.network

import android.content.Context
import science.credo.mobiledetector.App
import science.credo.mobiledetector.detector.Hit
import science.credo.mobiledetector.login.IdentityInfo
import science.credo.mobiledetector.login.LoginByEmailRequestBody
import science.credo.mobiledetector.login.LoginByUsernameRequestBody
import science.credo.mobiledetector.login.RegisterDeviceInfoRequestBody
import java.util.*


object RestInterface {


    suspend fun login(context: Context, login: String, password: String): Response {

        val info = IdentityInfo.getDefault(context).getIdentityData()

        val loginRequestBody = if ('@' in login) {
            LoginByEmailRequestBody.build(
                login,
                password,
                info
            )
        } else {
            LoginByUsernameRequestBody.build(
                login,
                password,
                info
            )
        }

        return Http.sendPostRequestRaw(Config.API_URL + "user/login", loginRequestBody)

    }

    suspend fun register(
        context: Context,
        login: String,
        email: String,
        password: String,
        displayName: String,
        team: String
    ): Response {

        val info = IdentityInfo.getDefault(context).getIdentityData()
        val registerRequestBody =
            RegisterDeviceInfoRequestBody.build(
                email,
                login,
                displayName,
                password,
                team,
                Locale.getDefault().language, //language_input.text.toString()
                info
            )

        return Http.sendPostRequestRaw(Config.API_URL + "user/register", registerRequestBody)

    }

    suspend fun sendHit(
        context: Context,
        hit: Hit
    ) :Response{
        val deviceInfo = IdentityInfo.getDefault(
            context
        ).getIdentityData()
        val request = DetectionRequestBody.build(
            deviceInfo, listOf(hit)
        )
        println("===========send token ${App.token}")
        return Http.sendPostRequestRaw(Config.API_URL + "detection", Pair("Authorization","Token ${App.token}"),request)
    }


}