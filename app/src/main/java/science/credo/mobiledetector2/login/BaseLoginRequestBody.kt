package science.credo.mobiledetector2.login

import science.credo.mobiledetector2.network.BaseDeviceInfoRequest

abstract class BaseLoginRequestBody : BaseDeviceInfoRequest() {
    abstract val password: String
}