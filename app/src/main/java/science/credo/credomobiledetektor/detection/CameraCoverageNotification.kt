package science.credo.credomobiledetektor.detection

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.media.MediaPlayer
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import science.credo.credomobiledetektor.R


/**
 * Created by Konrad on 2018-04-22.
 */

class CameraCoverageNotification(private val mContext: Context) {

    fun coverageNotify(){
        vibrate()
        beep()
        statusBar()
    }

    private fun statusBar() {
        val mNotification = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val mBuilder = NotificationCompat.Builder(mContext, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Uwaga kamera odsłonięta !!!")
                .setContentText("Upewnij sie ze komórka leży kamerą w stronę podłoża")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        mNotification.notify(0, mBuilder.build())
    }

    private fun beep(){
        val player = MediaPlayer.create(mContext, Settings.System.DEFAULT_NOTIFICATION_URI)
        player.start()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= 26) {
            (mContext.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            (mContext.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(200)
        }
    }

}