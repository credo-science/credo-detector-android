package science.credo.credomobiledetektor.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import science.credo.credomobiledetektor.MainActivity
import science.credo.credomobiledetektor.R


/**
 * Created by Konrad on 2018-05-11.
 */
class Alarm : BroadcastReceiver() {

    override fun onReceive(context : Context,intent : Intent) {
        val mNotification = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //Log.d("Brodcast","Alahrm")
        val launchNotifiactionIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context,
                0, launchNotifiactionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_info_black_24dp)
                .setContentTitle("CREDO")
                .setContentText("Experiment reminder")
                .setContentIntent(pendingIntent)

        mNotification.notify(0, builder.build())
    }
}