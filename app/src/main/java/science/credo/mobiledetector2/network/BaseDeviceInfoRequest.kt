package science.credo.mobiledetector2.network

abstract class BaseDeviceInfoRequest {
    abstract val device_id: String
    abstract val device_type: String
    abstract val device_model: String
    abstract val system_version: String
    abstract val app_version: String
}