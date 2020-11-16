package science.credo.mobiledetector2.settings

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