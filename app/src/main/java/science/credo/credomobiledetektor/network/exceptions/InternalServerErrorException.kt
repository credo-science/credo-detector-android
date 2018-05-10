package science.credo.credomobiledetektor.network.exceptions

class InternalServerErrorException(error: String) : ServerException(500, error)