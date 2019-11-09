package science.credo.mobiledetector.database

import android.content.Context
import com.instacart.library.truetime.TrueTime
import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.events.StatsValue

class DetectionStateWrapper(context: Context, val prefix: String) : SharedPreferencesWrapper(context) {
    companion object {
        fun getTotal(context: Context) : DetectionStateWrapper {
            return DetectionStateWrapper(context, "total")
        }

        fun getLatestSession(context: Context) : DetectionStateWrapper {
            return DetectionStateWrapper(context, "session")
        }

        fun getLatestPing(context: Context) : DetectionStateWrapper {
            return DetectionStateWrapper(context, "ping")
        }
    }

    var frameWidth: Int
        get() {
            return preferences.getInt("${prefix}_frame_width", 0)
        }
        set(v) {
            setInt("${prefix}_frame_width", v)
        }

    var frameHeight: Int
        get() {
            return preferences.getInt("${prefix}_frame_height", 0)
        }
        set(v) {
            setInt("${prefix}_frame_height", v)
        }

    var startDetectionTimestamp: Long
        get() {
            return preferences.getLong("${prefix}_start_detection_timestamp", 0)
        }
        set(v) {
            setLong("${prefix}_start_detection_timestamp", v)
        }

    var lastFlushTimestamp: Long
        get() {
            return preferences.getLong("${prefix}_last_flush_timestamp", 0)
        }
        set(v) {
            setLong("${prefix}_last_flush_timestamp", v)
        }

    var lastFrameAchievedTimestamp: Long
        get() {
            return preferences.getLong("${prefix}_last_frame_achieved_timestamp", 0)
        }
        set(v) {
            setLong("${prefix}_last_frame_achieved_timestamp", v)
        }

    var lastFramePerformedTimestamp: Long
        get() {
            return preferences.getLong("${prefix}_last_frame_performed_timestamp", 0)
        }
        set(v) {
            setLong("${prefix}_last_frame_performed_timestamp", v)
        }

    var lastHitTimestamp: Long
        get() {
            return preferences.getLong("${prefix}_last_hit_timestamp", 0)
        }
        set(v) {
            setLong("${prefix}_last_hit_timestamp", v)
        }

    var allFrames: Int
        get() {
            return preferences.getInt("${prefix}_all_frames", 0)
        }
        set(v) {
            setInt("${prefix}_all_frames", v)
        }

    var performedFrames: Int
        get() {
            return preferences.getInt("${prefix}_performed_frames", 0)
        }
        set(v) {
            setInt("${prefix}_performed_frames", v)
        }

    var onTime: Long
        get() {
            return preferences.getLong("${prefix}_on_time", 0)
        }
        set(v) {
            setLong("${prefix}_on_time", v)
        }

    var blacksStats : StatsValue
        get() {
            return getStatsValue("${prefix}_blacks")
        }
        set(v) {
            setStatsValue("${prefix}_blacks", v)
        }

    var averageStats : StatsValue
        get() {
            return getStatsValue("${prefix}_average")
        }
        set(v) {
            setStatsValue("${prefix}_average", v)
        }

    var maxStats : StatsValue
        get() {
            return getStatsValue("${prefix}_max")
        }
        set(v) {
            setStatsValue("${prefix}_max", v)
        }

    private fun getStatsValue(key: String) : StatsValue {
        return StatsValue(
                preferences.getFloat("${key}_min", 0f).toDouble(),
                preferences.getFloat("${key}_max", 0f).toDouble(),
                preferences.getFloat("${key}_average", 0f).toDouble(),
                preferences.getInt("${key}_samples", 0)
        )
    }

    private fun setStatsValue(key: String, v: StatsValue) {
        setFloat("${key}_min", v.min.toFloat() ?: 0f)
        setFloat("${key}_max", v.max.toFloat() ?: 0f)
        setFloat("${key}_average", v.average.toFloat() ?: 0f)
        setInt("${key}_samples", v.samples)
    }

    fun clear() {
        frameWidth = 0
        frameHeight = 0
        startDetectionTimestamp = 0
        lastFlushTimestamp = 0
        lastFrameAchievedTimestamp = 0
        lastFramePerformedTimestamp = 0
        lastHitTimestamp = 0
        allFrames = 0
        performedFrames = 0
        onTime = 0
        blacksStats = StatsValue()
        averageStats = StatsValue()
        maxStats = StatsValue()
    }

    fun merge(statsEvent: StatsEvent) {
        frameWidth = statsEvent.frameWidth
        frameHeight = statsEvent.frameHeight
        lastFlushTimestamp = TrueTime.now().time
        lastFrameAchievedTimestamp = statsEvent.lastFrameAchievedTimestamp
        lastFramePerformedTimestamp = statsEvent.lastFramePerformedTimestamp
        lastHitTimestamp = statsEvent.lastHitTimestamp
        allFrames += statsEvent.allFrames
        performedFrames += statsEvent.performedFrames
        onTime += statsEvent.onTime
        blacksStats = blacksStats.merge(statsEvent.blacksStats)
        averageStats = averageStats.merge(statsEvent.averageStats)
        maxStats = maxStats.merge(statsEvent.maxStats)
    }
}
