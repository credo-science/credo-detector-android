package science.credo.mobiledetector.detector

import android.graphics.Bitmap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.ByteArrayOutputStream

abstract class BaseFrameAnalyzer{
    abstract suspend fun checkHit(
        frame: Frame,
        frameResult: BaseFrameResult,
        calibration: BaseCalibrationResult
    ): Hit?

    suspend fun bitmap2png(bitmap: Bitmap): ByteArray {
        return GlobalScope.async {
            val pngData = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngData)
            return@async pngData.toByteArray()
        }.await()
    }
}