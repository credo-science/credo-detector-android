package science.credo.credomobiledetektor.network.messages

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey
import science.credo.credomobiledetektor.database.DetectionStateWrapper
import science.credo.credomobiledetektor.events.StatsEvent
import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Ping the server.
 *
 * @property delta_time Time since last detection / ping / startup.
 * @property timestamp UNIX timestamp of ping time.
 */
data class PingRequest(
        @PrimaryKey
        @AutoIncrement
        @JsonIgnore
        var id: Int = 0,

        override val device_id: String = "",
        override val device_type: String = "",
        override val device_model: String = "",
        override val system_version: String = "",
        override val app_version: String = "",

        val delta_time: Long = 0,
        val timestamp: Long = 0,

        var frame_width: Int = 0,
        var frame_height: Int = 0,

        var start_detection_timestamp: Long = 0,
        var last_flush_timestamp: Long = 0,
        var last_frame_achieved_timestamp: Long = 0,
        var last_frame_performed_timestamp: Long = 0,
        var last_hit_timestamp: Long = 0,

        var all_frames: Int = 0,
        var performed_frames: Int = 0,
        var on_time: Long = 0,

        var blacks_stats_min: Double = 0.0,
        var blacks_stats_max: Double = 0.0,
        var blacks_stats_average: Double = 0.0,
        var blacks_stats_samples: Int = 0,

        var average_stats_min: Double = 0.0,
        var average_stats_max: Double = 0.0,
        var average_stats_average: Double = 0.0,
        var average_stats_samples: Int = 0,

        var max_stats_min: Double = 0.0,
        var max_stats_max: Double = 0.0,
        var max_stats_average: Double = 0.0,
        var max_stats_samples: Int = 0
) : BaseDeviceInfoRequest()

fun build(timestamp: Long, deviceInfo: IdentityInfo.IdentityData, wrapper: DetectionStateWrapper) : PingRequest {
    return PingRequest(
            0,
            deviceInfo.device_id,
            deviceInfo.device_type,
            deviceInfo.device_model,
            deviceInfo.system_version,
            deviceInfo.app_version,
            wrapper.onTime,
            timestamp,
            wrapper.frameWidth,
            wrapper.frameHeight,
            wrapper.startDetectionTimestamp,
            wrapper.lastFlushTimestamp,
            wrapper.lastFrameAchievedTimestamp,
            wrapper.lastFramePerformedTimestamp,
            wrapper.lastHitTimestamp,
            wrapper.allFrames,
            wrapper.performedFrames,
            wrapper.onTime,
            wrapper.blacksStats.min,
            wrapper.blacksStats.max,
            wrapper.blacksStats.average,
            wrapper.blacksStats.samples,
            wrapper.averageStats.min,
            wrapper.averageStats.max,
            wrapper.averageStats.average,
            wrapper.averageStats.samples,
            wrapper.maxStats.min,
            wrapper.maxStats.max,
            wrapper.maxStats.average,
            wrapper.maxStats.samples
    )
}

fun build(timestamp: Long, deviceInfo: IdentityInfo.IdentityData, stats: StatsEvent) : PingRequest {
    return PingRequest(
            0,
            deviceInfo.device_id,
            deviceInfo.device_type,
            deviceInfo.device_model,
            deviceInfo.system_version,
            deviceInfo.app_version,
            stats.onTime,
            timestamp,
            stats.frameWidth,
            stats.frameHeight,
            stats.startDetectionTimestamp,
            stats.lastFlushTimestamp,
            stats.lastFrameAchievedTimestamp,
            stats.lastFramePerformedTimestamp,
            stats.lastHitTimestamp,
            stats.allFrames,
            stats.performedFrames,
            stats.onTime,
            stats.blacksStats.min,
            stats.blacksStats.max,
            stats.blacksStats.average,
            stats.blacksStats.samples,
            stats.averageStats.min,
            stats.averageStats.max,
            stats.averageStats.average,
            stats.averageStats.samples,
            stats.maxStats.min,
            stats.maxStats.max,
            stats.maxStats.average,
            stats.maxStats.samples
    )
}