package science.credo.mobiledetector.detector.old

import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import science.credo.mobiledetector.detector.FrameResult

object JniWrapper {

    var isBusy = false

    external fun calculateFrame(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        blackThreshold: Int
    ): String

    suspend fun calculateFrame(
        bytes: ByteArray,
        width: Int,
        height: Int,
        scaledWidth: Int,
        scaledHeight: Int,
        pixelPrecision: Int
    ): FrameResult {
        val time = TrueTimeRx.now().time
        isBusy = true
        return GlobalScope.async {
            val stringDataResult = calculateRawFrame(
                bytes,
                width,
                height,
                scaledWidth,
                scaledHeight,
                pixelPrecision
            )
            isBusy = false
            println("===============calc time ${TrueTimeRx.now().time - time}")
            return@async FrameResult.fromJniStringData(stringDataResult)
        }.await()
    }

    private external fun calculateRawFrame(
        bytes: ByteArray,
        width: Int,
        height: Int,
        scaledWidth: Int,
        scaledHeight: Int,
        pixelPrecision: Int
    ): String

    init {
        System.loadLibrary("kotlin-jni")
    }
}