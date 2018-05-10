package science.credo.credomobiledetektor.network.exceptions

class NotFoundException(error: String) : ServerException(404, error)