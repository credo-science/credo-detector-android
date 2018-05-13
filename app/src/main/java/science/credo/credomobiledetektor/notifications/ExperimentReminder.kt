package science.credo.credomobiledetektor.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Toast
import science.credo.credomobiledetektor.R
import java.util.*

/**
 * Created by Konrad on 2018-05-08.
 */

class ExperimentReminder constructor(context: Context) {

    private var mContext : Context ?= null

     init{
        this.mContext = context
     }

    fun scheduleAlarm(onOff : Boolean) {
        val alarmManager = mContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(mContext, Alarm::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(onOff){
            val calendar = Calendar.getInstance()
            calendar.setTimeInMillis(System.currentTimeMillis())

            val sharedPref = mContext?.getSharedPreferences("TimeAlarm",Context.MODE_PRIVATE) ?: return
            val hour = sharedPref.getInt("hour", 0)
            val minute = sharedPref.getInt("minute", 0)
            val res = mContext?.getResources()
            val schdStr = res?.getString(R.string.alarm_scheduled)
            val toast = Toast.makeText(mContext, schdStr + " " + hour.toString() + ":" + minute.toString(), Toast.LENGTH_LONG)
            toast.show()

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            Log.d("TimeAlarm",hour.toString())
            Log.d("TimeAlarm",minute.toString())

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmPendingIntent)
        }else{
            alarmManager.cancel(alarmPendingIntent)
            val toast = Toast.makeText(mContext, R.string.alarm_canceled, Toast.LENGTH_SHORT)
            toast.show()
        }
    }

}
