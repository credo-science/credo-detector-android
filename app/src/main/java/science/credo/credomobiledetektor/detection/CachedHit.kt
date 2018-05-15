package science.credo.credomobiledetektor.detection

import com.fasterxml.jackson.annotation.JsonAutoDetect
import science.credo.credomobiledetektor.info.HitInfo
import science.credo.credomobiledetektor.info.LocationInfo

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)

/**
 * Stores information about Hit synchronized with network.
 */
class CachedHit(
    frameInfo: HitInfo.FrameData,
    locationInfo: LocationInfo.LocationData,
    factorInfo: HitInfo.FactorData
) : Hit(frameInfo, locationInfo, factorInfo)
{
    constructor() : this(
        HitInfo.FrameData("", 0, 0, 0, 0, 0, 0, 0),
        LocationInfo.LocationData(0.0, 0.0, 0.0, 0f, "", 0),
        HitInfo.FactorData(0, 0, 0, 0)
    )
}
