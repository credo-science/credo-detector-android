package science.credo.mobiledetector.detection

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import java.io.IOException


class CameraSurfaceHolder(private val hCamera: Camera,
                          private val hServiceContext: Context) : SurfaceHolder.Callback {

    private val callbackBuffer1: ByteArray
    private val callbackBuffer2: ByteArray
    private val callbackBuffer3: ByteArray
    private val mCameraPreviewCallbackNative = CameraPreviewCallbackNative(hServiceContext)

    private val TAG = "CameraSurfaceHolder"

    init {
        var size = 0
        try {
            val parameters = hCamera.parameters
            val width = parameters.previewSize.width
            val height = parameters.previewSize.height
            size = width * height + width * height / 2
        } catch (e: Exception) {
            Log.w(TAG, "CameraSurfaceHolder init: failed to get preview size, fallback to small buffer", e)
            // fallback to small buffer to avoid crashes
            size = 1024
        }
        callbackBuffer1 = ByteArray(size)
        callbackBuffer2 = ByteArray(size)
        callbackBuffer3 = ByteArray(size)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

        Log.d(TAG, "surfaceCreated")

        try {
            hCamera.setPreviewDisplay(holder)
        } catch (e: Exception) {
            Log.w(TAG, e)
            return
        }

        try {
            hCamera.addCallbackBuffer(callbackBuffer1)
            hCamera.addCallbackBuffer(callbackBuffer2)
            hCamera.addCallbackBuffer(callbackBuffer3)

            hCamera.setPreviewCallbackWithBuffer(mCameraPreviewCallbackNative)

            hCamera.startPreview()
        } catch (e: Exception) {
            Log.w(TAG, "surfaceCreated: failed to start preview or add buffers", e)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed")
    }

    fun flush() {
        mCameraPreviewCallbackNative.flush()
    }
}

