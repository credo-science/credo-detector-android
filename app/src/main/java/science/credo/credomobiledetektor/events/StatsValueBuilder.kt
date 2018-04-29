package science.credo.credomobiledetektor.events

import java.util.*

class StatsValueBuilder {
    private val samples = LinkedList<Double>()

    fun addSample(sample: Double) : StatsValueBuilder {
        samples.add(sample);
        return this
    }

    fun toStatsValue() : StatsValue {
        val sv = StatsValue()
        sv.average = samples.average()
        sv.max = samples.max()
        sv.min = samples.min()
        sv.samples = samples.size
        return sv
    }
}
