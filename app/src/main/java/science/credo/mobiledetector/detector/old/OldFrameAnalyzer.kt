package science.credo.mobiledetector.detector.old

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import science.credo.mobiledetector.detector.*
import science.credo.mobiledetector.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector.utils.LocationHelper
import science.credo.mobiledetector.utils.SensorHelper
import java.io.ByteArrayOutputStream
import java.lang.IllegalStateException
import kotlin.math.max
import kotlin.math.min


object OldFrameAnalyzer : BaseFrameAnalyzer() {

    const val HIT_BITMAP_SIZE = 60

    override suspend fun checkHit(
        frame: Frame,
        frameResult: BaseFrameResult,
        calibration: BaseCalibrationResult
    ): Hit? {
        frameResult as OldFrameResult
        calibration as OldCalibrationResult
        val max = calibration.max

        if (frameResult.max > max) {

            val margin = HIT_BITMAP_SIZE / 2
            val centerX = frameResult.maxIndex.rem(frame.width)
            val centerY = frameResult.maxIndex / frame.width

            val offsetX = max(0, centerX - margin)
            val offsetY = max(0, centerY - margin)
            val endX = min(frame.width, centerX + margin)
            val endY = min(frame.height, centerY + margin)

            val cropBitmap = yuv2rgb(
                frame.byteArray,
                frame.width,
                frame.height,
                offsetX,
                offsetY,
                endX,
                endY
            )
            val cropDataPNG = bitmap2png(cropBitmap)
            val dataString = Base64.encodeToString(cropDataPNG, Base64.DEFAULT)

            val hit = Hit()
            hit.frameContent = dataString
            hit.timestamp = frame.timestamp
            hit.latitude = LocationHelper.location?.latitude
            hit.longitude = LocationHelper.location?.longitude
            hit.altitude = LocationHelper.location?.altitude
            hit.accuracy = LocationHelper.location?.accuracy
            hit.provider = LocationHelper.location?.provider
            hit.width = frame.width
            hit.height = frame.height
            hit.x = centerX
            hit.y = centerY
            hit.maxValue = frameResult.max
            if (calibration is OldCalibrationResult) {
                hit.blackThreshold = calibration.blackThreshold
            }
            hit.average = frameResult.avg.toFloat()
            hit.blacksPercentage = frameResult.blacksPercentage
            hit.ax = SensorHelper.accX
            hit.ay = SensorHelper.accY
            hit.az = SensorHelper.accZ
            hit.temperature = SensorHelper.temperature
            fillHited(frame,frameResult.maxIndex, HIT_BITMAP_SIZE)


            return hit
        } else {
            return null
        }
    }

    suspend fun fillHited(frame: Frame, maxPosition: Int, sideLength: Int) {

        //Point (maxX,maxY) is center(brightest pixel) of hit
        val maxX = maxPosition.rem(frame.width)
        val maxY = maxPosition / frame.width

        //Point (x,y) is upper-left corner of square with we want to fill
        var x = maxX - sideLength / 2
        var y = maxY - sideLength / 2


        when {
            x < 0 -> x = 0
            x >= frame.width - sideLength -> x = frame.width - sideLength
        }

        when {
            //We want to make sure that upper-left point of square is at least sideLength from bottom and right side of image
            y < 0 -> y = 0
            y >= frame.height - sideLength -> y = frame.height - sideLength
        }

        //Loops iterates from upper-left point sideLength times
        for (i in y..y + sideLength) {
            for (j in x..x + sideLength) {
                frame.byteArray[i * frame.width + j] = 0
            }
        }

    }


    suspend fun yuv2rgb(
        yuv: ByteArray,
        width: Int,
        height: Int,
        offsetX: Int,
        offsetY: Int,
        endX: Int,
        endY: Int
    ): Bitmap {
        return GlobalScope.async {
            val total = width * height
            val outWidth = endX - offsetX
            val outHeight = endY - offsetY

            val rgb = IntArray(outWidth * outHeight)

            var Y: Int
            var Cb = 0
            var Cr = 0

            var index = 0

            var R: Int
            var G: Int
            var B: Int

            for (y in offsetY until endY) {
                for (x in offsetX until endX) {
                    Y = yuv[y * width + x].toInt()
                    if (Y < 0) Y += 255

                    if (x and 1 == 0) {
                        Cr = yuv[(y shr 1) * width + x + total].toInt()
                        Cb = yuv[(y shr 1) * width + x + total + 1].toInt()

                        if (Cb < 0) Cb += 127 else Cb -= 128
                        if (Cr < 0) Cr += 127 else Cr -= 128
                    }

                    R = Y + Cr + (Cr shr 2) + (Cr shr 3) + (Cr shr 5)
                    G =
                        Y - (Cb shr 2) + (Cb shr 4) + (Cb shr 5) - (Cr shr 1) + (Cr shr 3) + (Cr shr 4) + (Cr shr 5)
                    B = Y + Cb + (Cb shr 1) + (Cb shr 2) + (Cb shr 6)

                    // Approximation
                    //				R = (int) (Y + 1.40200 * Cr);
                    //			    G = (int) (Y - 0.34414 * Cb - 0.71414 * Cr);
                    //				B = (int) (Y + 1.77200 * Cb);

                    if (R < 0) R = 0 else if (R > 255) R = 255
                    if (G < 0) G = 0 else if (G > 255) G = 255
                    if (B < 0) B = 0 else if (B > 255) B = 255

                    rgb[index++] = -0x1000000 + (R shl 16) + (G shl 8) + B
                }
            }

            return@async Bitmap.createBitmap(rgb, outWidth, outHeight, Bitmap.Config.ARGB_8888)
        }.await()

    }


}