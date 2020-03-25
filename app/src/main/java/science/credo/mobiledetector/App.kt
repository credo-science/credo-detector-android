package science.credo.mobiledetector

import android.app.Application
import science.credo.mobiledetector.utils.LocationHelper
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.utils.SensorHelper
import kotlin.system.exitProcess

class App : Application(), Thread.UncaughtExceptionHandler {

    companion object {
        lateinit var token: String
    }

    override fun onCreate() {
        super.onCreate()

        val savedToken = Prefs.get(this, String::class.java, Prefs.Keys.USER_TOKEN)
        if (savedToken?.isNotEmpty() == true) {
            token = savedToken
        }

        LocationHelper.init(this)
        SensorHelper.init(this)
        Thread.setDefaultUncaughtExceptionHandler(this)

    }

    override fun uncaughtException(t: Thread, e: Throwable) {

        exitProcess(0)

    }

}