package science.credo.mobiledetector.detection

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaRecorder
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import science.credo.mobiledetector.DetectorService
import science.credo.mobiledetector.R
import science.credo.mobiledetector.info.ConfigurationInfo
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


import android.hardware.camera2.CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_OFF
import android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_OFF
import android.hardware.camera2.CameraMetadata.HOT_PIXEL_MODE_OFF
import android.hardware.camera2.CameraMetadata.NOISE_REDUCTION_MODE_OFF
import android.hardware.camera2.CaptureRequest.*
import android.os.Handler
import android.os.HandlerThread


class Camera2SurfaceHolder(private val hServiceContext: Context)
    : CameraDevice.StateCallback(), BaseCameraSurfaceHolder {

    private var mCameraDevice: CameraDevice? = null
    private val mCameraOpenCloseLock = Semaphore(1)
    private var mSensorOrientation: Int? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var mVideoSize: Size? = null
    private var mImageReader: ImageReader? = null
    private var mPreviewSession: CameraCaptureSession? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private var mHolder: SurfaceHolder? = null
    private var mPreviewRequest: CaptureRequest? = null


    private var mCameraPreviewCallbackNative: Camera2Callback = Camera2Callback(hServiceContext)

    private val TAG = "CameraSurfaceHolder"

    override fun surfaceCreated(holder: SurfaceHolder) {

        Log.d(TAG, "surfaceCreated")
        mHolder = holder



        val manager = hServiceContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            startBackgroundThread()

            Log.d(DetectorService.TAG, "tryAcquire")
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            val cameraId = manager.cameraIdList[0]

            // Choose the sizes for camera preview and video recording
            val characteristics = manager.getCameraCharacteristics(cameraId)

            val map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            if (map == null) {
                throw RuntimeException("Cannot get available preview/video sizes")
            }

            val sizes = map.getOutputSizes(MediaRecorder::class.java)
            mVideoSize = if (ConfigurationInfo(hServiceContext).isFullFrame) sizes[sizes.size / 2] else sizes[sizes.size * 3 / 4]
            mImageReader = ImageReader.newInstance(mVideoSize!!.width,
                    mVideoSize!!.height,
                    ImageFormat.YUV_420_888, 3)
            mImageReader!!.setOnImageAvailableListener(mCameraPreviewCallbackNative, mBackgroundHandler)

            manager.openCamera(cameraId, this, mBackgroundHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Error Camera Open", e)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed")
    }

    override fun flush() {
        mCameraPreviewCallbackNative.flush()
        closeCamera()
    }

    override fun onOpened(camera: CameraDevice?) {
        Log.d(TAG, "onOpened")
        mCameraDevice = camera
        startPreview()
        mCameraOpenCloseLock.release()
    }

    override fun onDisconnected(camera: CameraDevice?) {
        Log.d(TAG, "onDisconnected")
        mCameraOpenCloseLock.release()
        camera!!.close()
        mCameraDevice = null
    }

    override fun onError(camera: CameraDevice?, error: Int) {
        Log.e(TAG, "Error Camera" + error)
        mCameraOpenCloseLock.release()
        camera!!.close()
        mCameraDevice = null
    }

    private fun startPreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            closePreviewSession()
            Log.d(TAG, "createCaptureRequest")
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
            Log.d(TAG, "createCaptureRequest done")
            mPreviewBuilder!!.set<Int>(CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON)
            /*mPreviewBuilder!!.set(SENSOR_EXPOSURE_TIME, 500000000L)
            mPreviewBuilder!!.set(CaptureRequest.SENSOR_FRAME_DURATION, 500000000L)*/
            mPreviewBuilder!!.set<Int>(NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_OFF)
            mPreviewBuilder!!.set<Int>(HOT_PIXEL_MODE, CameraMetadata.HOT_PIXEL_MODE_OFF)
            mPreviewBuilder!!.set<Int>(COLOR_CORRECTION_ABERRATION_MODE, COLOR_CORRECTION_ABERRATION_MODE_OFF)
            mPreviewBuilder!!.set(CaptureRequest.BLACK_LEVEL_LOCK, false)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_OFF)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
            //mPreviewBuilder.set(CaptureRequest.DISTORTION_CORRECTION_MODE, CaptureRequest.DISTORTION_CORRECTION_MODE_OFF);

            val surfaces = ArrayList<Surface>()

            val previewSurface = mHolder!!.surface
            surfaces.add(previewSurface)
            mPreviewBuilder!!.addTarget(previewSurface)

            val readerSurface = mImageReader!!.getSurface()
            surfaces.add(readerSurface)
            mPreviewBuilder!!.addTarget(readerSurface)

            mCameraDevice!!.createCaptureSession(surfaces,
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(session: CameraCaptureSession) {
                            mPreviewSession = session
                            updatePreview()
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e(TAG, "onConfigureFailed")
                        }
                    }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession!!.close()
            mPreviewSession = null
            //stopBackgroundThread()
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.getLooper())
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
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

    private fun updatePreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            Log.d(TAG, "updatePreview")
            mPreviewBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            val thread = HandlerThread("CameraPreview")
            thread.start()
            mPreviewRequest = mPreviewBuilder!!.build()
            mPreviewSession!!.setRepeatingRequest(mPreviewRequest, mCameraPreviewCallbackNative, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            closePreviewSession()
            if (null != mCameraDevice) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.")
        } finally {
            mCameraOpenCloseLock.release()
        }
    }
}
