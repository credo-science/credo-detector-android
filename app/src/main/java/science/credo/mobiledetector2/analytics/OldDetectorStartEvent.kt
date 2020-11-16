package science.credo.mobiledetector2.analytics

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import science.credo.mobiledetector2.detector.old.OldCalibrationResult
import science.credo.mobiledetector2.settings.BaseSettings


class OldDetectorStartEvent(
    context: Context,
    settings: BaseSettings,
    calibrationResult: OldCalibrationResult
) : DetectorStateEvent(context,settings,calibrationResult) {



    override val eventName: EventName
        get() = EventName.EVENT_OLD_DETECTOR_START
}