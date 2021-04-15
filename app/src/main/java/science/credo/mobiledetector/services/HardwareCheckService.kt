package science.credo.mobiledetector.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.IBinder
import android.util.Log
import science.credo.mobiledetector.CredoApplication
import science.credo.mobiledetector.DetectorService
import science.credo.mobiledetector.info.CameraSettings

class HardwareCheckService : Service() {
    companion object {
        val TAG = "HardwareCheckService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(DetectorService.TAG,"Start camera checking service...")

        val cs = checkCameraHardware(baseContext)
        val ca = application as CredoApplication
        ca.detectorMode = CredoApplication.DetectorMode.OFF
        ca.detectorRunning.set(false)
        ca.cameraSettings = cs

        Log.d(DetectorService.TAG,"Done of camera checking service...")

        return START_NOT_STICKY
    }

    private fun checkCameraHardware(context: Context): CameraSettings {
        val cs = CameraSettings()

        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            cs.numberOfCameras = Camera.getNumberOfCameras();
        } else {
            return cs
        }

        // Camera API v1
        for (i in 0 until cs.numberOfCameras) {
            val c = CameraSettings.Camera()
            cs.cameras.add(c)

            try {
                // Open camera for check
                val camera = Camera.open(i)
                val parameters: Camera.Parameters = camera.parameters;

                // Switch camera to more optimised mode for preview
                parameters.setRecordingHint(true)

                // Check supported size for preview
                val sizes = parameters.supportedPreviewSizes
                for (size in sizes) {
                    c.sizes.add(CameraSettings.Size(size.width, size.height))
                }

                // Close camera
                camera.release()
            } catch (e: RuntimeException) {
                if (CredoApplication.isEmulator()) {
                    cs.isEmulation = true
                } else {
                    c.errorMessage = e.message
                }
            }
        }

        return cs
    }
}