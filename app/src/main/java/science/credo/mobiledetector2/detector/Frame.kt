package science.credo.mobiledetector2.detector

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

data class Frame(
    val byteArray: ByteArray,
    val width: Int,
    val height: Int,
    val imageFormat: Int,
    val exposureTime: Long,
    val timestamp: Long)

{
    suspend fun saveToStorage(context: Context) {

        val storage = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val folder = storage!!.path + "/detections"
        val timestampFolder = File("$folder/$timestamp/")
        println("=========save frame $timestampFolder   ${timestampFolder.exists()}")
        if (!timestampFolder.exists()) {
            timestampFolder.mkdirs()
        }
        val file = File(timestampFolder.path+"/frame")

        var fos: FileOutputStream? = null

        try {
            fos = FileOutputStream(file)
            // Writes bytes from the specified byte array to this file output stream
            fos.write(byteArray)
        } catch (e: FileNotFoundException) {
            println("File not found$e")
        } catch (ioe: IOException) {
            println("Exception while writing file $ioe")
        } finally { // close the streams using close method
            try {
                if (fos != null) {
                    fos.close()
                }
            } catch (ioe: IOException) {
                println("Error while closing stream: $ioe")
            }
        }

    }

}