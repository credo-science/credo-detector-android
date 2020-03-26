package science.credo.mobiledetector

import android.app.Application
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import science.credo.mobiledetector.utils.LocationHelper
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.utils.SensorHelper
import kotlin.system.exitProcess

class App : MultiDexApplication(){

    companion object {
        lateinit var token: String
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        val savedToken = Prefs.get(this, String::class.java, Prefs.Keys.USER_TOKEN)
        if (savedToken?.isNotEmpty() == true) {
            token = savedToken
        }

        LocationHelper.init(this)
        SensorHelper.init(this)

    }



}