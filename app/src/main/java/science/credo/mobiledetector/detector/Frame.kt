package science.credo.mobiledetector.detector

data class Frame(
    val byteArray: ByteArray,
    val width: Int,
    val height: Int,
    val imageFormat: Int,
    val exposureTime: Long,
    val timestamp: Long)