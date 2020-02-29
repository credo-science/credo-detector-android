package science.credo.mobiledetector.synchronization

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class UpdateTimeBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(contxt: Context, intent: Intent?) {
        val disposable = TrueTimeRx.build()
                .withConnectionTimeout(31428)
                .withRetryCount(100)
                .withSharedPreferencesCache(contxt)
                .initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ date ->
                    Log.d("UpdateTimeReceiver", "TrueTime synchronized successfully ")
                }, {
                    Log.e("UpdateTimeReceiver", "something went wrong when trying to initializeRx TrueTime", it)
                })
        logTrueTime("UpdateTimeReceiver")
        setAlarm(contxt)
    }

    fun setAlarm(context: Context) {
        val mIntent = Intent(context, UpdateTimeBroadcastReceiver::class.java)
        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val mPendingIntent = PendingIntent.getBroadcast(context, 0, mIntent, 0)
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + 120000L), mPendingIntent)
    }

    fun toastTrueTime(contxt: Context) {
        val time_diffrence_millis = System.currentTimeMillis() - TrueTimeRx.now().time
        val time_diffrence_result = String.format("%02dh:%02dm:%02ds:%04dms",
                TimeUnit.MILLISECONDS.toHours(time_diffrence_millis),
                TimeUnit.MILLISECONDS.toMinutes(time_diffrence_millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time_diffrence_millis)),
                TimeUnit.MILLISECONDS.toSeconds(time_diffrence_millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time_diffrence_millis)),
                TimeUnit.MILLISECONDS.toMillis(time_diffrence_millis) -
                        TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(time_diffrence_millis)))

        Toast.makeText(contxt, "" + time_diffrence_result, Toast.LENGTH_LONG).show()
    }

    fun logTrueTime(tag: String) {
        val time_diffrence_millis = System.currentTimeMillis() - TrueTimeRx.now().time
        val time_diffrence_result = String.format("%02dh:%02dm:%02ds:%04dms",
                TimeUnit.MILLISECONDS.toHours(time_diffrence_millis),
                TimeUnit.MILLISECONDS.toMinutes(time_diffrence_millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time_diffrence_millis)),
                TimeUnit.MILLISECONDS.toSeconds(time_diffrence_millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time_diffrence_millis)),
                TimeUnit.MILLISECONDS.toMillis(time_diffrence_millis) -
                        TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(time_diffrence_millis)))

        Log.d(tag,"Difference between local time and ntp time " + time_diffrence_result
        )
    }
}

