package science.credo.mobiledetector2.detector

//import androidx.room.Entity
import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import science.credo.mobiledetector2.network.RestInterface
import java.io.File
import java.io.PrintWriter

//import androidx.room.PrimaryKey


//@Entity(tableName = "old_hit_table")
class Hit(
) {

    //    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @SerializedName("frame_content")
    var frameContent: String? = null
    var timestamp: Long? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var altitude: Double? = null
    var accuracy: Float? = null
    var provider: String? = null
    var width: Int? = null
    var height: Int? = null
    var x: Int? = null
    var y: Int? = null
    @SerializedName("max")
    var maxValue: Int? = null
    var average: Float? = null
    @SerializedName("blacks")
    var blacksPercentage: Float? = null
    @SerializedName("black_threshold")
    var blackThreshold: Int? = null
    var ax: Float? = null
    var ay: Float? = null
    var az: Float? = null
    var orientation: Float? = null
    var temperature: Int? = null

    var format: String? = null
    var processingMethod: String? = null
    var clusteringFactor: String? = null
    var calibrationNoise: Int? = null
    var threshold: Int? = null
    var thresholdAmplifier: Float? = null
    var exposure :Long? =null


    suspend fun send(context: Context) {

        println("============send")
        val response = RestInterface.sendHit(context, this)
        println("=================send response ${response.getCode()}")
        println("=================send response ${response.isSuccess()}")
        println("=================send response ${response.getResponse()}")

    }

    suspend fun saveToStorage(context: Context) {

        val storage = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val folder = storage!!.path + "/detections"
        val timestampFolder = File("$folder/$timestamp/")
        println("=========$timestampFolder   ${timestampFolder.exists()}")
        if (!timestampFolder.exists()) {
            timestampFolder.mkdirs()
        }
        var counter = 0
        var detectionFile: File? = null
        do {
            counter++
            detectionFile = File("$folder/$timestamp/${timestamp}_$counter.txt")
        } while (detectionFile!!.exists())

        detectionFile.createNewFile()
        val writer = PrintWriter(detectionFile.path, "UTF-8")
        writer.println(Gson().toJson(this))
        writer.close()

        println("============send")

    }
}