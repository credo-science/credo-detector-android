package science.credo.credomobiledetektor.events

import java.util.*

class StatsValueBuilder {
    private val samples = LinkedList<Double>()

    fun addSample(sample: Double) : StatsValueBuilder {
        samples.add(sample)
        return this
    }

    fun toStatsValue() : StatsValue {
        val sv = StatsValue()
        sv.average = samples.average()
        sv.max = samples.max() ?: 0.0
        sv.min = samples.min() ?: 0.0
        sv.samples = samples.size
        return sv
    }
}
