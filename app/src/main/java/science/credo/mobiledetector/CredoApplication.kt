package science.credo.mobiledetector

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.multidex.MultiDex
import com.instacart.library.truetime.TrueTime
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.sender.HttpSender
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.events.DetectorStateEvent
import science.credo.mobiledetector.info.CameraSettings
import science.credo.mobiledetector.info.ConfigurationInfo
import science.credo.mobiledetector.services.HardwareCheckService
import science.credo.mobiledetector.services.LocationCheckService
import java.util.concurrent.atomic.AtomicBoolean

@AcraCore(buildConfigClass = BuildConfig::class)
@AcraHttpSender(uri = "https://api.credo.science/acra/report",
        httpMethod = HttpSender.Method.POST)
class CredoApplication : Application() {

    enum class DetectorMode(val value: Int) {
        OFF(0), CHECK(1), CALIBRATION(2), DETECTION(3)
    }

    var cameraSettings: CameraSettings? = null
    val detectorRunning = AtomicBoolean(false)
    var detectorMode: DetectorMode = DetectorMode.OFF
    var detectorState: DetectorStateEvent = DetectorStateEvent(false)

    protected override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        // The following line triggers the initialization of ACRA
        ACRA.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        if (!ConfigurationWrapper(this).autoRun) {
            ConfigurationInfo(this).isDetectionOn = false
        }
        if (ConfigurationInfo(this).isDetectionOn) {
            turnOnDetection()
        }

        doAsync {
            TrueTime.build().initialize();
        }

        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionCode = info.versionCode
    }

    fun turnOnDetection(mode: DetectorMode = DetectorMode.DETECTION) {
        if (detectorRunning.compareAndSet(false, true)) {
            detectorMode = DetectorMode.CHECK
            if (mode == DetectorMode.CHECK) {
                startService(Intent(this, HardwareCheckService::class.java))
                startService(Intent(this, LocationCheckService::class.java))
            } else if (mode == DetectorMode.DETECTION || mode == DetectorMode.CALIBRATION) {
                startService(Intent(this, DetectorService::class.java))
            }
        }
    }

    fun turnOffDetection() {
        stopService(Intent(this, DetectorService::class.java))
        detectorRunning.set(false)
        detectorMode = DetectorMode.OFF
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDetectorStateChange(detectorStateEvent: DetectorStateEvent) {
        detectorState = detectorStateEvent
        detectorRunning.set(detectorStateEvent.running)
    }

    companion object {
        var versionCode: Int = 0

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
