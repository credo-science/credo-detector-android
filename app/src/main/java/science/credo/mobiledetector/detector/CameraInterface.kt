package science.credo.mobiledetector.detector

import android.content.Context

abstract class CameraInterface {

    interface FrameCallback {
        fun onFrameReceived(frame: Frame)
    }

    abstract fun start(context: Context)

    abstract fun stop()

}