package science.credo.mobiledetector2.detector.old

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
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
        blackThreshold: Int,
        imageFormat: Int
    ): OldFrameResult {
        return GlobalScope.async {
            val time = TrueTimeRx.now().time
            isBusy = true

            val stringData: String
            if (imageFormat == ImageFormat.JPEG) {
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                stringData = GlobalScope.async {
                    return@async calculateRGBFrame(
                        pixels,
                        width,
                        height,
                        blackThreshold
                    )
                }.await()
            } else {
                stringData = GlobalScope.async {
                    return@async calculateOldFrame(
                        byteArray,
                        width,
                        height,
                        blackThreshold
                    )
                }.await()
            }

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

    private external fun calculateRGBFrame(
        intArray: IntArray,
        width: Int,
        height: Int,
        blackThreshold: Int
    ): String

    init {
        System.loadLibrary("kotlin-jni")
    }
}