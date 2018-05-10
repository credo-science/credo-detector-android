package science.credo.credomobiledetektor.events

data class StatsValue (
        var min: Double = 0.0,
        var max: Double = 0.0,
        var average: Double = 0.0,
        var samples: Int = 0
) {
    fun merge(v: StatsValue) {
        min = Math.min(min, v.min)
        max = Math.max(max, v.max)
        average = (average * samples + v.average * v.samples)/(samples + v.samples).toDouble()
        samples += v.samples
    }
}
