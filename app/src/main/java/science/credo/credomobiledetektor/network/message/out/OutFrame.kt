package science.credo.credomobiledetektor.network.message.out

import science.credo.credomobiledetektor.network.message.FrameOutHeader

/**
 * Created by poznan on 04/09/2017.
 */

open class OutFrame (type: String) {
    val header = FrameOutHeader(type)
}


