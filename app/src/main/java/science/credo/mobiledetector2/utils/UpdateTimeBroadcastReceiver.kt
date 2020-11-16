package science.credo.mobiledetector2.utils


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.instacart.library.truetime.TrueTime.clearCachedInfo
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class UpdateTimeBroadcastReceiver : BroadcastReceiver() {

    var isSynchronized = false
    // ToDo Set flag Synchronized for Hits

    override fun onReceive(contxt: Context, intent: Intent?) {
        initRxTrueTime(contxt)
        logTrueTime("UpdateTimeReceiver")
        toastTrueTime(contxt)
        setAlarm(contxt)
    }

    @SuppressLint("CheckResult")
    fun initRxTrueTime(contxt: Context) {

        var mTrueTimeRx = TrueTimeRx.build()
            .withLoggingEnabled(true)
            .withSharedPreferencesCache(contxt)

        if (isConnectedToNetwork(contxt)) {
            if (TrueTimeRx.isInitialized()) {
                clearCachedInfo()
            }

            mTrueTimeRx.initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ date ->
                    Log.d("initRxTrueTime", "initRxTrueTime - Success initialized TrueTime :$date")
                }, {
                    Log.e(
                        "initRxTrueTime",
                        "something went wrong when trying to initializeRx TrueTime",
                        it
                    )
                })

            isSynchronized = true

        } else {
            isSynchronized = false

            if (!TrueTimeRx.isInitialized()) {
//                We are not synchronized
//                ToDo Will crash when app launch without internet. Remedy = Force internet in LaunchActivity -> is forced already due to LoginProcess
            } else {
//                We are not synchronized, but latest drift is in cache
            }
        }


    }

    fun setAlarm(context: Context) {
        val mIntent = Intent(context, UpdateTimeBroadcastReceiver::class.java)
        val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val mPendingIntent = PendingIntent.getBroadcast(context, 0, mIntent, 0)
        mAlarmManager.set(
            AlarmManager.RTC_WAKEUP,
            (System.currentTimeMillis() + 120000L),
            mPendingIntent
        )
    }

    fun toastTrueTime(contxt: Context) {
        if (isSynchronized) {
            val time_diffrence_millis = System.currentTimeMillis() - TrueTimeRx.now().time
            val time_diffrence_result = String.format(
                "%02dh:%02dm:%02ds:%04dms",
                TimeUnit.MILLISECONDS.toHours(time_diffrence_millis),
                TimeUnit.MILLISECONDS.toMinutes(time_diffrence_millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time_diffrence_millis)),
                TimeUnit.MILLISECONDS.toSeconds(time_diffrence_millis) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                time_diffrence_millis
                            )
                        ),
                TimeUnit.MILLISECONDS.toMillis(time_diffrence_millis) -
                        TimeUnit.SECONDS.toMillis(
                            TimeUnit.MILLISECONDS.toSeconds(
                                time_diffrence_millis
                            )
                        )
            )
            Toast.makeText(contxt, "" + time_diffrence_result, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(contxt, "Internet not avaiable, you are not synchronized with TimeServer", Toast.LENGTH_LONG).show()
        }
    }

    fun logTrueTime(tag: String) {
        if (isSynchronized) {
            val time_diffrence_millis = System.currentTimeMillis() - TrueTimeRx.now().time
            val time_diffrence_result = String.format(
                "%02dh:%02dm:%02ds:%04dms",
                TimeUnit.MILLISECONDS.toHours(time_diffrence_millis),
                TimeUnit.MILLISECONDS.toMinutes(time_diffrence_millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time_diffrence_millis)),
                TimeUnit.MILLISECONDS.toSeconds(time_diffrence_millis) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                time_diffrence_millis
                            )
                        ),
                TimeUnit.MILLISECONDS.toMillis(time_diffrence_millis) -
                        TimeUnit.SECONDS.toMillis(
                            TimeUnit.MILLISECONDS.toSeconds(
                                time_diffrence_millis
                            )
                        )
            )

            Log.d(
                tag, "Difference between local time and ntp time " + time_diffrence_result
            )
        } else {
            Log.w(tag, "Internet not avaiable, you are not synchronized with TimeServer")
        }

    }
}


fun isConnectedToNetwork(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw      = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}