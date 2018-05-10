package science.credo.credomobiledetektor.network.exceptions

open class ServerException(val code: Int, val error: String) : Exception(error)
