package science.credo.mobiledetector.detector

import android.content.Context

abstract class CameraInterface {

    interface FrameCallback {
        fun onFrameReceived(frame: Frame, sameFrameTimestamp: Long?)
        fun onFrameReceived(frame: Frame){
            onFrameReceived(frame,null)
        }
    }

    abstract fun start(context: Context)

    abstract fun stop()

}