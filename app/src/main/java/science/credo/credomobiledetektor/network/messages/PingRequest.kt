package science.credo.credomobiledetektor.network.messages

import science.credo.credomobiledetektor.database.DetectionStateWrapper
import science.credo.credomobiledetektor.events.StatsEvent
import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Ping the server.
 *
 * @property delta_time Time since last detection / ping / startup.
 * @property timestamp UNIX timestamp of ping time.
 */
class PingRequest(
    val delta_time: Long,
    val timestamp: Long,

    var frame_width: Int,
    var frame_height: Int,

    var start_detection_timestamp: Long,
    var last_flush_timestamp: Long,
    var last_frame_achieved_timestamp: Long,
    var last_frame_performed_timestamp: Long,
    var last_hit_timestamp: Long,

    var all_frames: Int,
    var performed_frames: Int,
    var on_time: Long,

    var blacks_stats_min: Double,
    var blacks_stats_max: Double,
    var blacks_stats_average: Double,
    var blacks_stats_samples: Int,

    var average_stats_min: Double,
    var average_stats_max: Double,
    var average_stats_average: Double,
    var average_stats_samples: Int,

    var max_stats_min: Double,
    var max_stats_max: Double,
    var max_stats_average: Double,
    var max_stats_samples: Int,

    deviceInfo: IdentityInfo.IdentityData
) : BaseRequest (deviceInfo) {
    companion object {
        fun build(timestamp: Long, deviceInfo: IdentityInfo.IdentityData, wrapper: DetectionStateWrapper) : PingRequest {
            return PingRequest(
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
                    wrapper.maxStats.samples,
                    deviceInfo
            )
        }

        fun build(timestamp: Long, deviceInfo: IdentityInfo.IdentityData, stats: StatsEvent) : PingRequest {
            return PingRequest(
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
                    stats.maxStats.samples,
                    deviceInfo
            )
        }
    }
}
