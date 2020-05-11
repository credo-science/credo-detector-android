package science.credo.mobiledetector.login

import science.credo.mobiledetector.network.BaseDeviceInfoRequest

abstract class BaseLoginCodeRequestBody : BaseDeviceInfoRequest() {
    abstract val authorization_code: String
}