package science.credo.credomobiledetektor.network.message.out

import android.os.Build
import android.provider.Settings.Secure
import science.credo.credomobiledetektor.info.IdentityInfo

/**
 * Created by poznan on 04/09/2017.
 */

open class OutFrame (deviceInfo: IdentityInfo.IdentityData) {
    val device_id:      String = deviceInfo.device_id
    val device_type:    String = deviceInfo.device_type
    val device_model:   String = deviceInfo.device_model
    val system_version: String = deviceInfo.system_version
    val app_version:    String = deviceInfo.app_version
}


