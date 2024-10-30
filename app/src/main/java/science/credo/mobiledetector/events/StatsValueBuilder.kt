package science.credo.mobiledetector.events

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
        val samples = LinkedList<Double>()
        sv.max = samples.maxOrNull() ?: 0.0
        sv.min = samples.minOrNull() ?: 0.0
        sv.samples = samples.size
        return sv
    }
}
