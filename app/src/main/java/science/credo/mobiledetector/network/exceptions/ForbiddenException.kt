package science.credo.mobiledetector.network.exceptions

class ForbiddenException(error: String) : ServerException(403, error)