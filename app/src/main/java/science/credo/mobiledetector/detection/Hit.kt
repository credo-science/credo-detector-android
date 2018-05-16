package science.credo.mobiledetector.detection

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
class Hit (frameContent: String,
           timestamp: Long,
           latitude: Double,
           longitude: Double,
           altitude: Double,
           accuracy: Float,
           provider: String,
           width: Int,
           height: Int,
           x: Int,
           y: Int,
           maxValue: Int,
           average: Double,
           blacks: Double,
           blackThreshold: Int,
           ax: Float,
           ay: Float,
           az: Float,
           orientation: Float,
           temperature: Int
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
    @JsonProperty("x")
    val mX: Int = x
    @JsonProperty("y")
    val mY: Int = y
    @JsonProperty("max")
    val mMaxValue: Int = maxValue
    @JsonProperty("average")
    val mAverage: Double = average
    @JsonProperty("blacks")
    val mBlacks: Double = blacks
    @JsonProperty("black_threshold")
    val mBlackThreshold: Int = blackThreshold
    @JsonProperty("ax")
    val mAx: Float = ax
    @JsonProperty("ay")
    val mAy: Float = ay
    @JsonProperty("az")
    val mAz: Float = az
    @JsonProperty("orientation")
    val mOrientation: Float = orientation
    @JsonProperty("temperature")
    val mTemperature: Int = temperature

    @JsonIgnore
    var toSent = true

    @JsonIgnore
    var serverId = 0L

    @JsonIgnore
    val detectionTimestamp = (System.currentTimeMillis() / 10000L).toInt() // PultusORM less condition walkaround

    constructor() : this("", 0, 0.0, 0.0, 0.0, 0.0f, "", 0, 0, 0, 0, 0, 0.0, 0.0, 0, 0f, 0f, 0f, 0f, 0) {}
}
