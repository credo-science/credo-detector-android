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
        imageFormat: Int,
        colorFilterArrangement: Int?,
        whiteLevel: Int?,
        blackLevelArray: IntArray?
    ): OldFrameResult {
        return GlobalScope.async {
            val time = TrueTimeRx.now().time
            isBusy = true

            val stringData: String
            when (imageFormat) {
                ImageFormat.JPEG -> {
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
                }

                ImageFormat.RAW_SENSOR -> {
                    stringData = GlobalScope.async {
                        return@async calculateRawSensorFrame(
                            byteArray,
                            width,
                            height,
                            blackThreshold,
                            colorFilterArrangement!!,
                            whiteLevel!!,
                            blackLevelArray!!
                        )
                    }.await()
                }

                else -> {
                    stringData = GlobalScope.async {
                        return@async calculateOldFrame(
                            byteArray,
                            width,
                            height,
                            blackThreshold
                        )
                    }.await()
                }
            }

            println("debug $stringData")

            val result = OldFrameResult.fromJniStringData(stringData, whiteLevel, blackLevelArray)
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
            println("debug $stringData")
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

    private external fun calculateRawSensorFrame(
        byteArray: ByteArray,
        width: Int,
        height: Int,
        blackThreshold: Int,
        colorFilterArrangement: Int,
        whiteLevel: Int,
        blackLevelArray: IntArray
    ): String

    init {
        System.loadLibrary("kotlin-jni")
    }
}