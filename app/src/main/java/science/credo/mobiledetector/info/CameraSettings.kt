package science.credo.mobiledetector.info

import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CameraSettings {
    class Size(val width: Int, val height: Int)

    class Camera {
        val sizes = ArrayList<Size>(0)
        var errorMessage: String? = null
        var maxRes: Int? = null
        var halfRes: Int? = null

        fun computeMaxHalfRes() {
            val map = HashMap<Int, Int>(0)
            for (i in 0 until sizes.size) {
                val size = sizes[i]
                val mul = size.height * size.width
                map[mul] = i
            }
            val sorted = map.toSortedMap()
            val maxVal = sorted.lastKey()
            maxRes = sorted.getValue(maxVal)
            for (s in sorted.keys.reversed()) {
                if ((s < maxVal / 4) or (s < 640*480)) {
                    break
                }
                halfRes = sorted.getValue(s)
            }
        }
    }

    var isEmulation: Boolean = false
    var numberOfCameras: Int = 0

    val cameras = ArrayList<Camera>()
}
