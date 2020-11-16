package science.credo.mobiledetector2

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import science.credo.mobiledetector2.utils.LocationHelper
import science.credo.mobiledetector2.utils.Prefs
import science.credo.mobiledetector2.utils.SensorHelper

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