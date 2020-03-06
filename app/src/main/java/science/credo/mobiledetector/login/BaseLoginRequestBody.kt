package science.credo.mobiledetector.login

import science.credo.mobiledetector.network.BaseDeviceInfoRequest

abstract class BaseLoginRequestBody : BaseDeviceInfoRequest() {
    abstract val password: String
}