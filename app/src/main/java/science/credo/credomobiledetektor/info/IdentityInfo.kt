package science.credo.credomobiledetektor.info

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Secure.getString

/**
 * Created by poznan on 24/08/2017.
 */

class IdentityInfo (mContext: Context?){

    data class IdentityData (val deviceId: String, val androidVersion: String, val deviceModel: String){}

    val deviceId: String = getString(mContext?.getContentResolver(), Settings.Secure.ANDROID_ID);
    val androidVersion: String = "${Build.VERSION.SDK_INT}-${Build.VERSION.RELEASE}"
    val deviceModel: String = Build.MODEL

    fun getIdentityData(): IdentityData = IdentityData(deviceId, androidVersion, deviceModel)
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

