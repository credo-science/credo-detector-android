package science.credo.mobiledetector.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import java.util.*

@SuppressLint("HardwareIds")
class IdentityInfo(mContext: Context) {

    // FIXME: Build.SERIAL is deprecated bu Build.getSerial() needs
    // controversial privilege Manifest.permission.READ_PHONE_STATE
    private val device_id: String


    /*if (Build.VERSION.SDK_INT >= 26) {
        Build.getSerial()
    } else {
        Build.SERIAL
    }*/

    private val deviceType: String = Build.DEVICE
    private val deviceModel: String = Build.MODEL
    private val systemVersion: String = "${Build.VERSION.SDK_INT}-${Build.VERSION.RELEASE}"
    private val appVersion: String

    init {

        val pInfo = mContext.packageManager.getPackageInfo(mContext.packageName, 0)
        appVersion = pInfo.versionName

        val telephonyManager =
            (mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)

        device_id = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.imei
            } else {
                telephonyManager.deviceId
            }
        } catch (e: SecurityException) {
            "UNKNOWN"
        }

    }

    fun getIdentityData(): IdentityData =
        IdentityData(device_id, deviceType, deviceModel, systemVersion, appVersion)

    data class IdentityData(
        val device_id: String = "",
        val device_type: String = "",
        val device_model: String = "",
        val system_version: String = "",
        val app_version: String = ""
    )

    companion object {
        fun getDefault(context: Context): IdentityInfo {
            return IdentityInfo(context)
        }
    }
}
