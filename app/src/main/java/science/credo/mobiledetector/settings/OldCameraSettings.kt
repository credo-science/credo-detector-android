package science.credo.mobiledetector.settings

class OldCameraSettings(
    format: Int,
    width: Int,
    height: Int,
    val fpsRange: IntArray
) : BaseSettings(
    format,
    width,
    height
)