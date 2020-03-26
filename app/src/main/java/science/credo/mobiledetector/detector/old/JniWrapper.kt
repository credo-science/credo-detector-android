package science.credo.mobiledetector.detector.old

import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import science.credo.mobiledetector.detector.OldFrameResult
import science.credo.mobiledetector.detector.camera2.RawFormatFrameResult

object JniWrapper {

    var isBusy = false


    suspend fun calculateFrame(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        blackThreshold: Int
    ): OldFrameResult {
        return GlobalScope.async {
            val time = TrueTimeRx.now().time
            isBusy = true
            val stringData = GlobalScope.async {
                return@async calculateOldFrame(
                    byteArray,
                    width,
                    height,
                    blackThreshold
                )
            }.await()
            val result = OldFrameResult.fromJniStringData(stringData)
            isBusy = false
            println("===============calc time ${TrueTimeRx.now().time - time}")
            return@async result
        }.await()
    }


    suspend fun calculateFrame(
        bytes: ByteArray,
        width: Int,
        height: Int,
        scaledWidthFactor: Int,
        scaledHeightFactor: Int,
        pixelPrecision: Int
    ): RawFormatFrameResult {
        return GlobalScope.async {
            val time = TrueTimeRx.now().time
            isBusy = true
            val stringData = GlobalScope.async {
                return@async calculateRawFrame(
                    bytes,
                    width,
                    height,
                    width / scaledWidthFactor,
                    height / scaledHeightFactor,
                    pixelPrecision
                )
            }.await()
            val result = RawFormatFrameResult.fromJniStringData(stringData)
            isBusy = false
            println("===============calc time ${TrueTimeRx.now().time - time}")
            return@async result
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