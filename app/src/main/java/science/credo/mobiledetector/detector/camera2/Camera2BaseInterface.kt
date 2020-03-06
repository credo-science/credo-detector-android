package science.credo.mobiledetector.detector.camera2

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.RequiresApi
import science.credo.mobiledetector.detector.CameraInterface
import java.lang.Exception


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
abstract class Camera2BaseInterface : CameraInterface(), ImageReader.OnImageAvailableListener {

    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    var context: Context? = null
    var cam: CameraDevice? = null
    var mImageReader: ImageReader? = null
    var session: CameraCaptureSession? = null

    lateinit var captureBuilder: CaptureRequest.Builder


    fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    fun stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread!!.quitSafely()
            try {
                mBackgroundThread!!.join()
                mBackgroundThread = null
                mBackgroundHandler = null
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun getBackgroundHandler(): Handler? {
        return mBackgroundHandler
    }

    abstract fun startRepeatingRequest(session: CameraCaptureSession)

    override fun stop() {

        try {
            session?.stopRepeating()
            session?.abortCaptures()
            session?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            cam?.close()
            mImageReader?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}


