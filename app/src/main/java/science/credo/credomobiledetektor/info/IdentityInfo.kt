package science.credo.credomobiledetektor.info

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Secure.getString

class IdentityInfo (mContext: Context?){

    data class IdentityData (val device_id: String, val device_type: String, val device_model: String, val system_version: String, val app_version: String)

    val device_id:      String = getString(mContext?.getContentResolver(), Settings.Secure.ANDROID_ID)
    val device_type:    String = Build.DEVICE
    val device_model:   String = Build.MODEL
    val system_version: String = "${Build.VERSION.SDK_INT}-${Build.VERSION.RELEASE}"
    val app_version:    String = "1.0"

    fun getIdentityData(): IdentityData = IdentityData(device_id, device_type, device_model, system_version, app_version)
    fun getIdentityString(): String = getIdentityData().toString()

    companion object {
        private var mIdentityInfo: IdentityInfo? = null;
        fun getInstance (context: Context) : IdentityInfo {
            if (mIdentityInfo == null) {
                mIdentityInfo = IdentityInfo(context)
            }
           return mIdentityInfo!!
        }
    }
}

