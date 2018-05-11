package science.credo.credomobiledetektor.info

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.BatteryManager
import android.preference.PreferenceManager
import android.util.Log


/**
 * Created by poznan on 26/08/2017.
 * Modified by nkg on 17/03/2018
 */

class ConfigurationInfo (context: Context) {

    val TAG = "ConfigurationInfo"
    val mContext: Context = context
    val mConnectManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

   /* // If not null -> user logged
    val isLogged: String
        get() = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString("LOGIN_TOKEN", null);
*/
    val isChargerOnly: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean("process_charging_only",true)

    val isWifiOnly: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean("upload_wifi_only",true)

    val cropSize: Int
        get() = parseIntPref(mContext, "crop", 20)

    val maxFactor: Int
        get() = parseIntPref(mContext, "max", 120)

    val averageFactor: Int
        get() = parseIntPref(mContext, "average", 30)

    val blackFactor: Int
        get() = parseIntPref(mContext, "black", 0)

    val blackCount: Int
        get() = parseIntPref(mContext, "count", 0)

    val maxTemperature: Int
        get() = parseIntPref(mContext, "temperature", 60)

    val batteryLevel: Int
        get() = parseIntPref(mContext, "level", 60)

    val stopAfter: Int
        get() = parseIntPref(mContext, "stop_after", 0)

    val pauseTime: Int
        get() = parseIntPref(mContext, "pause_time", 0)

    val isFullFrame: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean("full_frame",false)


    val isCharging: Boolean
        get() {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = mContext.registerReceiver(null, ifilter)
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            Log.d(TAG, "BATTERY_STATUS_CHARGING: ${status == BatteryManager.BATTERY_STATUS_CHARGING}")
            Log.d(TAG, "BATTERY_STATUS_FULL: ${status == BatteryManager.BATTERY_STATUS_FULL}")
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }

    val isPlugged: Boolean
        get() {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = mContext.registerReceiver(null, ifilter)
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            return status > 0
        }

    fun getCaller() : String{
        val stacktrace = Thread.currentThread().stackTrace
        val e = stacktrace[4]//maybe this number needs to be corrected
        return "${e.className}.${e.methodName}"
    }

    val canProcess: Boolean
        get() {
            val cp = isDetectionOn && (!isChargerOnly || (isChargerOnly && isPlugged))
            Log.d(TAG, "canProcess: $cp (from: ${getCaller()})")
            return cp
        }

    val isConnected: Boolean
        get() {
            val activeNetwork: NetworkInfo? = mConnectManager.activeNetworkInfo
            val connected = activeNetwork?.isConnectedOrConnecting
            return activeNetwork != null || connected ?: false
        }

    val isWifiConnected: Boolean
        get() {
            val activeNetwork: NetworkInfo = mConnectManager.activeNetworkInfo
            return activeNetwork.type == ConnectivityManager.TYPE_WIFI
        }

    val canUpload: Boolean
        get() = isWifiConnected || (isConnected && !isWifiOnly)

    var isDetectionOn : Boolean
        get() {
            Log.d(TAG, "isDetectionOn.get  (from: ${getCaller()})")
            val pm = PreferenceManager.getDefaultSharedPreferences(mContext)
            return pm.getBoolean("preference_detection_on", false)
        }
        set(v: Boolean) {
            Log.d(TAG, "isDetectionOn.set: $v  (from: ${getCaller()})")
            val pm = PreferenceManager.getDefaultSharedPreferences(mContext)
            pm.edit().putBoolean("preference_detection_on", v).apply()
        }

    data class ConfigurationData (val isChargerOnly:Boolean,
                                  val isWifiOnly:Boolean,
                                  val isCharging:Boolean,
                                  val canProcess:Boolean,
                                  val isConnected:Boolean,
                                  val isWifiConnected:Boolean,
                                  val canUpload:Boolean){

    }

    fun getConfigurationData(): ConfigurationData = ConfigurationData(isChargerOnly, isWifiOnly, isCharging, canProcess, isConnected, isWifiConnected, canUpload)

    companion object {
        fun parseIntPref(context: Context, name: String, defVal: Int) : Int {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(name, "").toIntOrNull() ?: return defVal
        }
    }
}