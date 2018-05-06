package science.credo.credomobiledetektor.network.exceptions

class UnauthorizedException(error: String) : ServerException(401, error)