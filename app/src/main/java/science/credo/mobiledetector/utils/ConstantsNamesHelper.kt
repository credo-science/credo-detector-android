package science.credo.mobiledetector.utils

import android.graphics.ImageFormat

object ConstantsNamesHelper {

    fun getFormatName(v: Int): String? {

        return when (v) {
            17 -> "NV21"
            32 -> "RAW_SENSOR"
            34 -> "PRIVATE"
            35 -> "YUV_420_888"
            36 -> "RAW_PRIVATE"
            256 -> "JPEG"
            842094169 -> "YV12"
            else -> null
        }
    }

}