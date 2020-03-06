package science.credo.mobiledetector.detector

//import androidx.room.Entity
import android.content.Context
import com.google.gson.annotations.SerializedName
import science.credo.mobiledetector.network.RestInterface

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


    suspend fun send(context: Context){

        RestInterface.sendHit(context,this)

    }
}