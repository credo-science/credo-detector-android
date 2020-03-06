package science.credo.mobiledetector.detector


class FrameResult(
    val avg: Int,
    val blacksPercentage: Float,
    val max: Int,
    val maxIndex: Int
) {
    companion object {
        fun fromJniStringData(data: String): FrameResult {
            val parts = data.split(";")
            return FrameResult(
                parts[0].toInt(),
                parts[1].toFloat(),
                parts[2].toInt(),
                parts[3].toInt()
            )
        }
    }
}