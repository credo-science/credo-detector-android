package science.credo.credomobiledetektor.detection

import android.graphics.Bitmap

/**
 * Source: https://github.com/yushulx/NV21-to-RGB/blob/master/src/com/main/NV21.java
 */
object ImageConversion {
    fun yuv2rgb(yuv: ByteArray, width: Int, height: Int, offsetX: Int, offsetY: Int, endX: Int, endY: Int): Bitmap {
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
                G = Y - (Cb shr 2) + (Cb shr 4) + (Cb shr 5) - (Cr shr 1) + (Cr shr 3) + (Cr shr 4) + (Cr shr 5)
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

        return Bitmap.createBitmap(rgb, outWidth, outHeight, Bitmap.Config.ARGB_8888)
    }
}
