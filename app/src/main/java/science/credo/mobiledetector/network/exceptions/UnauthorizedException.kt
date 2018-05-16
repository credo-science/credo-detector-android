package science.credo.mobiledetector.network.exceptions

class UnauthorizedException(error: String) : ServerException(401, error)