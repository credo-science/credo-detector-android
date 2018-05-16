package science.credo.mobiledetector.network.exceptions

class NotFoundException(error: String) : ServerException(404, error)