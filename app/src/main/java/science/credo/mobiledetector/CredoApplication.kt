package science.credo.mobiledetector

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.multidex.MultiDex
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.sender.HttpSender
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.database.DataManager
import science.credo.mobiledetector.events.DetectorStateEvent
import science.credo.mobiledetector.info.ConfigurationInfo
import java.util.concurrent.atomic.AtomicBoolean

@AcraCore(buildConfigClass = BuildConfig::class)
@AcraHttpSender(uri = "https://api.credo.science/acra/report",
        httpMethod = HttpSender.Method.POST)
class CredoApplication : Application() {

    val globalDeviceState = GlobalDeviceState()

    @Deprecated("TODO: migrate to state from globalDeviceState")
    val detectorRunning = AtomicBoolean(false)
    var detectorState: DetectorStateEvent = DetectorStateEvent(false)

    protected override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        // The following line triggers the initialization of ACRA
        ACRA.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("native-lib")
        EventBus.getDefault().register(this)
        if (!ConfigurationWrapper(this).autoRun) {
            ConfigurationInfo(this).isDetectionOn = false
        }
        if (ConfigurationInfo(this).isDetectionOn) {
            turnOnDetection()
        }
    }

    fun turnOnDetection() {
        if (detectorRunning.compareAndSet(false, true)) {
            startService(Intent(this, DetectorService::class.java))
        }
    }

    fun turnOffDetection() {
        stopService(Intent(this, DetectorService::class.java))
        detectorRunning.set(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDetectorStateChange(detectorStateEvent: DetectorStateEvent) {
        detectorState = detectorStateEvent
        detectorRunning.set(detectorStateEvent.running)
    }

    companion object {
        fun isEmulator(): Boolean {
            return (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || "google_sdk" == Build.PRODUCT)
        }
    }
}
