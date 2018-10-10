package science.credo.mobiledetector.info

import android.content.Context
import android.os.Build

class IdentityInfo (mContext: Context) {

    // FIXME: Build.SERIAL is deprecated bu Build.getSerial() needs
    // controversial privilege Manifest.permission.READ_PHONE_STATE
    private val device_id: String = Build.SERIAL
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

        val pInfo = mContext.packageManager.getPackageInfo(mContext.packageName, 0);
        appVersion = "${pInfo.versionCode}"
    }

    fun getIdentityData(): IdentityData = IdentityData(device_id, deviceType, deviceModel, systemVersion, appVersion)

    data class IdentityData (
            val device_id: String = "",
            val device_type: String = "",
            val device_model: String = "",
            val system_version: String = "",
            val app_version: String = ""
    )

    companion object {
        fun getDefault(context: Context) : IdentityInfo {
            return IdentityInfo(context)
        }
    }
}
