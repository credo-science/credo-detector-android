package science.credo.credomobiledetektor.network.message

/**
 * Created by poznan on 06/09/2017.
 */
class FrameOutHeader(type:String) {
    val protocol = "1.0"
    val application = "1.0"
    val frame_type = type
    val time_stamp = System.currentTimeMillis();
}