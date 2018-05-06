package science.credo.credomobiledetektor.info

class HitInfo() {
    data class FrameData(
        val frameContent: String,
        val width: Int,
        val height: Int,
        val max: Int,
        val average: Int,
        val x: Int,
        val y: Int,
        val blackCount: Int
    )

    data class FactorData(
        val max: Int,
        val average: Int,
        val black: Int,
        val blackCount: Int
    )
}
