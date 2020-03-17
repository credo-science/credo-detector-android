package science.credo.mobiledetector.detector

import android.content.Context
import science.credo.mobiledetector.utils.Prefs

abstract class BaseCalibrationResult {

    fun save(context: Context) {
        println("=====save calibration ${this::class.java}")
        Prefs.put(context, this)
    }

}