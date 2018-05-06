package science.credo.credomobiledetektor.network.exceptions

class ForbiddenException(error: String) : ServerException(403, error)