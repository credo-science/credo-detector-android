package science.credo.mobiledetector.network.exceptions

class BadRequestException(error: String) : ServerException(400, error)