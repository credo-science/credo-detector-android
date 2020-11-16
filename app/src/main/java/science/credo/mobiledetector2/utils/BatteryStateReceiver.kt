package science.credo.mobiledetector2.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class BatteryStateReceiver : BroadcastReceiver() {

    var applicationJustStarted = true
    var chargerOn = true
    var fullCharge = true
   companion object{
       var lastKnownLevel: Int? = null
       var lastKnownTemperature: Int? = null
   }

    override fun onReceive(context: Context?, intent: Intent?) {
        val i = intent!!
        val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        if (!applicationJustStarted) {
            val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val temperature = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            lastKnownLevel = level
            lastKnownTemperature = temperature
            val lowBatteryLevel = 100f
            val batteryLevel = level / scale.toFloat() * 100
            if (status == BatteryManager.BATTERY_STATUS_DISCHARGING && batteryLevel < lowBatteryLevel) {
                chargerOn = false
                fullCharge = false
            } else if (status == BatteryManager.BATTERY_STATUS_CHARGING && level > 0) {

            } else if (status == BatteryManager.BATTERY_STATUS_FULL && !fullCharge) {
                fullCharge = true
            }
        } else {
            applicationJustStarted = false
        }
    }
}