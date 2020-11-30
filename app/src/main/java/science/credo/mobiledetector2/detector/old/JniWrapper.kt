package science.credo.mobiledetector2.detector.old

import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import science.credo.mobiledetector2.detector.OldFrameResult
import science.credo.mobiledetector2.detector.camera2.RawFormatFrameResult

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

    // This function may or may not follow the wrong algorithm
    private fun calculateOldFrame(
        byteArray: ByteArray, // A list of greyscale image bytes from 0 (black) to 127 (white)
        width: Int,
        height: Int,
        blackThreshold: Int
    ): String {
        var sum: Int = 0
        var blackPixels: Int = 0
        var max: Int = 0
        var maxIndex: Int = 0

        byteArray.forEachIndexed { index, byte ->
            sum += byte

            if (byte > max) {
                max = byte.toInt()
                maxIndex = index
            }

            if (byte < blackThreshold) {
                ++blackPixels
            }
        }

        return "${sum / byteArray.size};$blackPixels;${byteArray.size};$max;$maxIndex"
    }

    init {
        System.loadLibrary("kotlin-jni")
    }
}