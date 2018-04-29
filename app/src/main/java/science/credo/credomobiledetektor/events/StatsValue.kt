package science.credo.credomobiledetektor.events

data class StatsValue (
        var min: Double? = 0.0,
        var max: Double? = 0.0,
        var average: Double = 0.0,
        var samples: Int = 0
)
