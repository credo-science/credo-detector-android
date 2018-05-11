package science.credo.credomobiledetektor.detection

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey
import science.credo.credomobiledetektor.info.HitInfo
import science.credo.credomobiledetektor.info.LocationInfo

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)

open class Hit(
    val frameInfo: HitInfo.FrameData,
    val locationInfo: LocationInfo.LocationData,
    val factorInfo: HitInfo.FactorData
) {
    @PrimaryKey
    @AutoIncrement
    @JsonProperty("id")
    var id: Int = 0
    @JsonProperty("timestamp")
    val mTimestamp: Long = locationInfo.timestamp
    // Location information
    @JsonProperty("latitude")
    val mLatitude: Double = locationInfo.latitude
    @JsonProperty("longitude")
    val mLongitude: Double = locationInfo.longitude
    @JsonProperty("altitude")
    val mAltitude: Double = locationInfo.altitude
    @JsonProperty("accuracy")
    val mAccuracy: Float = locationInfo.accuracy
    @JsonProperty("provider")
    val mProvider: String = locationInfo.provider

    // Frame information
    @JsonProperty("frame_content")
    val mFrameContent: String = frameInfo.frameContent
    @JsonProperty("frame_width")
    val mFrameWidth: Int = frameInfo.width
    @JsonProperty("frame_height")
    val mFrameHeight: Int = frameInfo.height
    @JsonProperty("frame_max")
    val mFrameMax: Int = frameInfo.max
    @JsonProperty("frame_average")
    val mFrameAverage: Int = frameInfo.average
    @JsonProperty("frame_x")
    val mFrameX: Int = frameInfo.x
    @JsonProperty("frame_y")
    val mFrameY: Int = frameInfo.y
    @JsonProperty("frame_black_count")
    val mFrameBlackCount: Int = frameInfo.blackCount

    // Factor information
    @JsonProperty("factor_max")
    val mFactorMax: Int = factorInfo.max
    @JsonProperty("factor_average")
    val mFactorAverage: Int = factorInfo.average
    @JsonProperty("factor_black")
    val mFactorBlack: Int = factorInfo.black
    @JsonProperty("factor_black_count")
    val mFactorBlackCount: Int = factorInfo.blackCount

    // Battery information
    @JsonProperty("working_time")
    val mWorkingTime: Int = 0
    @JsonProperty("battery_level")
    val mBatteryLevel: Int = 0
    @JsonProperty("is_battery_charging")
    val mIsBatteryCharging: Boolean = false

    constructor() : this(
        HitInfo.FrameData("", 0, 0, 0, 0, 0, 0, 0),
        LocationInfo.LocationData(0.0, 0.0, 0.0, 0f, "", 0),
        HitInfo.FactorData(0, 0, 0, 0)
    )
}
