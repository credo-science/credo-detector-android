package science.credo.mobiledetector.utils

import java.util.*

class Statistics(internal var data: IntArray) {
    internal var size: Int = 0

    internal val mean: Double
        get() {
            var sum = 0.0
            for (a in data)
                sum += a
            return sum / size
        }

    internal val variance: Double
        get() {
            val mean = mean
            var temp = 0.0
            for (a in data)
                temp += (a - mean) * (a - mean)
            return temp / (size - 1)
        }

    internal val stdDev: Double
        get() = Math.sqrt(variance)

    init {
        size = data.size
    }

    fun median(): Double {
        Arrays.sort(data)
        return if (data.size % 2 == 0) (data[data.size / 2 - 1] + data[data.size / 2]) / 2.0 else data[data.size / 2].toDouble()
    }
}