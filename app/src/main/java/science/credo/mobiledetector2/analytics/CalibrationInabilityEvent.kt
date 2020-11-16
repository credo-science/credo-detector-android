package science.credo.mobiledetector2.analytics

import android.content.Context
import science.credo.mobiledetector2.settings.BaseSettings

class CalibrationInabilityEvent(
    context: Context,
    settings: BaseSettings
)  : DetectorStateEvent(context,settings, null){
    override val eventName: EventName
        get() = EventName.EVENT_CALIBRATION_INABILITY

}