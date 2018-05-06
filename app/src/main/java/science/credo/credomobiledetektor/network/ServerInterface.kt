package science.credo.credomobiledetektor.network

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import science.credo.credomobiledetektor.network.exceptions.*
import science.credo.credomobiledetektor.network.messages.*

class ServerInterface {
    private val mMapper = jacksonObjectMapper()

    private inline fun <reified T: Any> sendAndGetResponse(endpoint: String, outFrame: Any): T {
        val response = NetworkCommunication.post(endpoint, mMapper.writeValueAsString(outFrame))
        when(response.code) {
            in 200..299 -> return mMapper.readValue(response.message)
            else -> throw throwError(response)
        }
    }

    private fun sendAndGetNoContent(endpoint: String, outFrame: Any) {
        val response = NetworkCommunication.post(endpoint, mMapper.writeValueAsString(outFrame))
        when(response.code) {
            in 200..299 -> return
            else -> throw throwError(response)
        }
    }

    private fun throwError(response: NetworkCommunication.Response) : Exception {
        val message = extractJsonMessage(response.message)

        return when(response.code) {
            400 -> BadRequestException(message)
            401 -> UnauthorizedException(message)
            403 -> ForbiddenException(message)
            404 -> NotFoundException(message)
            500 -> InternalServerErrorException(message)
            else -> ServerException(response.code, message)
        }
    }

    private fun extractJsonMessage(message: String): String {
        try {
            return mMapper.readValue<ErrorMessage>(message).message
        } catch (e:Exception) {
            return message
        }
    }

    fun login(request: BaseLoginRequest) : LoginResponse {
        return sendAndGetResponse("/user/login", request)
    }

    fun register(request: RegisterRequest) {
        return sendAndGetNoContent("/user/register", request)
    }

    companion object {
        fun getDefault() : ServerInterface {
            return ServerInterface()
        }
    }

    class ErrorMessage(val message: String)
}
