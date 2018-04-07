package science.credo.credomobiledetektor.network.message.`in`

/**
 * Created by poznan on 28/09/2017.
 */

// post result: 400 {"error":"ERR_USEREXISTS","message":"User has been already registered."}

class ErrorFrame {
    val error: String = ""
    val message: String = ""
}