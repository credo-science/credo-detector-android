package science.credo.mobiledetector.utils

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.nio.ByteBuffer

object BitmapUtils {

    fun createBitmap(
        bitmapArray: IntArray,
        pixelPrecission: Int,
        width: Int,
        height: Int
    ): Deferred<Bitmap> {

        return GlobalScope.async {

            val time = System.nanoTime()

            for (i in 0 until bitmapArray.size) {
                val byte = bitmapArray[i]

                val red = byte shl 16 and 0x00FF0000
                val green = byte shl 8 and 0x0000FF00
                val blue = byte and 0x000000FF
                val result: Int = red or green or blue
                bitmapArray[i] = result
            }


            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            bitmap.setPixels(bitmapArray, 0, width, 0, 0, width, height)
            return@async bitmap
        }
    }

     suspend fun createBitmap(data: ByteArray, pixelPrecission: Int, width: Int, height: Int): Bitmap {

        return GlobalScope.async {
            val bb = ByteBuffer.wrap(data)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            bitmap.copyPixelsFromBuffer(bb)
            return@async bitmap
        }.await()

    }

     fun createCropped(bitmap: Bitmap, startColumn: Int, startRow: Int, size: Int): Deferred<Bitmap> {
        return GlobalScope.async {
            return@async Bitmap.createBitmap(bitmap, startColumn, startRow, size, size)
        }
    }
}
