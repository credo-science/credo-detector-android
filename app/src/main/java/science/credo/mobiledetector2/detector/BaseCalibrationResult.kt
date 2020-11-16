package science.credo.mobiledetector2.detector

import android.content.Context
import science.credo.mobiledetector2.utils.Prefs

abstract class BaseCalibrationResult {

    fun save(context: Context) {
        println("=====save calibration ${this::class.java}")
        Prefs.put(context, this)
    }

}