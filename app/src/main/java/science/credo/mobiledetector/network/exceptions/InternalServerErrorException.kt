package science.credo.mobiledetector.network.exceptions

class InternalServerErrorException(error: String) : ServerException(500, error)