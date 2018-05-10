package science.credo.credomobiledetektor.detection

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey

/**
 * Created by poznan on 27/08/2017.
 */

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
class Hit (frameContent: String,
           timestamp: Long,
           latitude: Double,
           longitude: Double,
           altitude: Double,
           accuracy: Float,
           provider: String,
           width: Int,
           height: Int
           ){
    @PrimaryKey
    @AutoIncrement
    @JsonProperty("id")
    var id: Int = 0
    @JsonProperty("frame_content")
    val mFrameContent: String = frameContent
    @JsonProperty("timestamp")
    val mTimestamp: Long = timestamp
    @JsonProperty("latitude")
    val mLatitude: Double = latitude
    @JsonProperty("longitude")
    val mLongitude: Double = longitude
    @JsonProperty("altitude")
    val mAltitude: Double = altitude
    @JsonProperty("accuracy")
    val mAccuracy: Float = accuracy
    @JsonProperty("provider")
    val mProvider: String = provider
    @JsonProperty("height")
    val mHeight: Int = height
    @JsonProperty("width")
    val mWidth: Int = width


    constructor() : this("", 0, 0.0, 0.0, 0.0, 0.0f, "", 0, 0) {}
}
