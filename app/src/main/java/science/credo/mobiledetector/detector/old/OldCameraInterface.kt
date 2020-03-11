package science.credo.mobiledetector.detector.old

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.Camera
import android.os.Build
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import com.instacart.library.truetime.TrueTimeRx
import science.credo.mobiledetector.detector.CameraInterface
import science.credo.mobiledetector.detector.Frame
import science.credo.mobiledetector.settings.OldCameraSettings

class OldCameraInterface(
    val frameCallback: FrameCallback,
    val settings: OldCameraSettings
) : CameraInterface(), Camera.PreviewCallback {

    private var mCamera: Camera? = null;
    private var mSurfaceView: SurfaceView? = null
    private var mWindowManager: WindowManager? = null
    private var surfaceHolderCallback: SurfaceHolder.Callback? = null

    override fun start(context: Context) {

        mCamera = Camera.open()
        val parameters: Camera.Parameters = mCamera!!.parameters;
        parameters.setRecordingHint(true)
        parameters.setPreviewSize(settings.width, settings.height)
        parameters.previewFormat = settings.imageFormat
        parameters.setPreviewFpsRange(settings.fpsRange[0], settings.fpsRange[1])
        mCamera?.parameters = parameters

        mSurfaceView = SurfaceView(context)
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            2,
            2,
            -5000,
            5000,
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        surfaceHolderCallback = object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mCamera?.setPreviewDisplay(holder)
                mCamera?.setPreviewCallback(this@OldCameraInterface)
                mCamera?.startPreview()
                startTS = TrueTimeRx.now().time
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {

            }
        }

        mSurfaceView?.holder?.addCallback(surfaceHolderCallback)

        mWindowManager?.addView(mSurfaceView, params)
        mSurfaceView?.setZOrderOnTop(false)
        mSurfaceView?.visibility = View.VISIBLE
//        setState(DetectorStateEvent.StateType.Normal, getString(R.string.status_fragment_running))

    }

    var counter = 0.0
    var startTS = 0L

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

        val ts = TrueTimeRx.now().time
        counter++
        val exposure = 1000 / (counter / ((TrueTimeRx.now().time - startTS) / 1000))
        if (data != null) {
            frameCallback.onFrameReceived(
                Frame(
                    data,
                    settings.width,
                    settings.height,
                    settings.imageFormat,
                    exposure.toLong(),
                    ts
                )
            )

        }

    }


    override fun stop() {
        mSurfaceView?.holder?.removeCallback(surfaceHolderCallback)
        mWindowManager?.removeView(mSurfaceView)
        mCamera?.stopPreview()
        mCamera?.release()
    }

}