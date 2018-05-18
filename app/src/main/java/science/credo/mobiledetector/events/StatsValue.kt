package science.credo.mobiledetector.events

import android.util.Log

data class StatsValue (
        var min: Double = 0.0,
        var max: Double = 0.0,
        var average: Double = 0.0,
        var samples: Int = 0
) {
    fun merge(v: StatsValue): StatsValue {
        Log.i("MERGE before", "${min} vs ${v.min}, ${max} vs ${v.max}, ${average} vs ${v.average}, ${samples} vs ${v.samples}")
        min = Math.min(min, v.min)
        max = Math.max(max, v.max)
        average = (average * samples + v.average * v.samples)/(samples + v.samples).toDouble()
        samples += v.samples
        Log.i("MERGE after", "${min} vs ${v.min}, ${max} vs ${v.max}, ${average} vs ${v.average}, ${samples} vs ${v.samples}")
        return this
    }
}
