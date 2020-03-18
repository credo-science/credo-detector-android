package science.credo.mobiledetector.detector.old

import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import science.credo.mobiledetector.detector.BaseFrameResult
import science.credo.mobiledetector.detector.OldFrameResult
import science.credo.mobiledetector.detector.camera2.RawFormatFrameResult

object JniWrapper {

    var isBusy = false


    fun calculateFrame(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        blackThreshold: Int
    ): OldFrameResult {
        return OldFrameResult.fromJniStringData(
            calculateOldFrame(
                byteArray,
                width,
                height,
                blackThreshold
            )
        )
    }

    suspend fun calculateFrame(
        bytes: ByteArray,
        width: Int,
        height: Int,
        scaledWidthFactor: Int,
        scaledHeightFactor: Int,
        pixelPrecision: Int
    ): RawFormatFrameResult {
        val time = TrueTimeRx.now().time
        isBusy = true
        return GlobalScope.async {
            val stringDataResult = calculateRawFrame(
                bytes,
                width,
                height,
                width / scaledWidthFactor,
                height / scaledHeightFactor,
                pixelPrecision
            )
            isBusy = false
            println("===============calc time ${TrueTimeRx.now().time - time}")
            return@async RawFormatFrameResult.fromJniStringData(stringDataResult)
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

    private external fun calculateOldFrame(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        blackThreshold: Int
    ): String

    init {
        System.loadLibrary("kotlin-jni")
    }
}