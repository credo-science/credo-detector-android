package science.credo.mobiledetector.info

import kotlin.collections.ArrayList

class CameraSettings {
    class Size(val width: Int, val height: Int)

    class Camera {
        val sizes = ArrayList<Size>(0)
        var errorMessage: String? = null
    }

    var isEmulation: Boolean = false
    var numberOfCameras: Int = 0

    val cameras = ArrayList<Camera>()
}
