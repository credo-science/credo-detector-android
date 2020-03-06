package science.credo.mobiledetector

import android.app.Application
import science.credo.mobiledetector.utils.LocationHelper
import science.credo.mobiledetector.utils.SensorHelper

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LocationHelper.init(this)
        SensorHelper.init(this)

    }

}