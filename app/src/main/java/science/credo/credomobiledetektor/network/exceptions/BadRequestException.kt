package science.credo.credomobiledetektor.network.exceptions

class BadRequestException(error: String) : ServerException(400, error)