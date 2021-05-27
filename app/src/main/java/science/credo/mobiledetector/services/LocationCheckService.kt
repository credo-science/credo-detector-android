package science.credo.mobiledetector.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import okhttp3.Request
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.DetectorService
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.network.NetworkCommunication

class LocationCheckService : Service() {
    companion object {
        val TAG = "LocationCheckService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun byIpNeedUpdate(oldIp: String, newIp: String): Boolean {
        val old = oldIp.split('.')
        val current = newIp.split('.')

        if ((current.size != 4) || (old.size != 4)) {
            return true
        }

        return (old[0] != current[0]) && (old[1] != current[1])
    }

    private fun byTimestampNeedUpdate(oldTime: Long): Boolean {
        return oldTime + 7 * 24 * 60 * 60 * 1000 < System.currentTimeMillis()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(DetectorService.TAG,"Start location checking service...")
        val cw = ConfigurationWrapper(this)

        doAsync {

            val request = Request.Builder().url("https://api.ipify.org/").build()
            val response = NetworkCommunication.client.newCall(request).execute()
            val responseString = response.body()?.string() ?: ""

            if (byIpNeedUpdate(cw.localizationIP, responseString)) {
                cw.localizationNeedUpdate = 2
            } else if (byTimestampNeedUpdate(cw.localizationTimestamp)) {
                cw.localizationNeedUpdate = 1
            }
        }

        return START_NOT_STICKY
    }
}
