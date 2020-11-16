package science.credo.mobiledetector2.detector.camera2

import android.content.Context
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi
import com.instacart.library.truetime.TrueTimeRx
import science.credo.mobiledetector2.detector.Frame
import science.credo.mobiledetector2.settings.Camera2ApiSettings
import science.credo.mobiledetector2.utils.SynchronizedTimeUtils
import java.util.ArrayList

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2PostConfigurationInterface(
    private val settings: Camera2ApiSettings,
    private val callback: FrameCallback
) : Camera2BaseInterface() {

    private val TAG = Camera2PostConfigurationInterface::class.java.simpleName
    var lastFrameTimestamp: Long = 0

    override fun onImageAvailable(p0: ImageReader?) {
        val timestamp = SynchronizedTimeUtils.getTimestamp()
        val exposure = timestamp - lastFrameTimestamp
        lastFrameTimestamp = timestamp

        val image = p0!!.acquireLatestImage()
        if (image == null) {
            println("=============image null !? ")
            return
        }


        println("=====================ts image ${image.timestamp}")
        println("=====================ts elapsed realtime  ${TrueTimeRx.now().time}")

//        val d = image.timestamp / 1000 - SntpClient.ntpTimeReference

//        val c = Calendar.getInstance()
//        c.timeInMillis = timestamp
//        println("==============on Image Available ${c.get(Calendar.HOUR_OF_DAY)}:${c.get(Calendar.MINUTE)}:${c.get(Calendar.SECOND)}.${c.get(Calendar.MILLISECOND)}     ||| $timestamp")

        val imageFormat = p0.imageFormat
        val buffer1 = image.planes[0].buffer
        var data: ByteArray? = null
        data = ByteArray(buffer1.remaining())
        buffer1.get(data)
        val frame = Frame(
            data,
            image.width,
            image.height,
            imageFormat,
            exposure,
            timestamp
        )

        callback.onFrameReceived(frame)
        image.close()
    }


    override fun start(context: Context) {
        println("$TAG ==== width ===> ${settings.width}")
        println("$TAG ==== height ===> ${settings.height}")

        this.context = context

        startBackgroundThread()

        val cameraManager: CameraManager =
            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = settings.cameraId

        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onError(p0: CameraDevice, p1: Int) {

                    println("$TAG   onError:$p1")
                }

                override fun onOpened(cameraDevice: CameraDevice) {


                    cam = cameraDevice
                    captureBuilder = cameraDevice.createCaptureRequest(settings.template)
                    captureBuilder.set<Int>(CaptureRequest.CONTROL_AE_MODE, settings.controlAEMode)

                    captureBuilder.set<Int>(CaptureRequest.CONTROL_MODE, settings.controlMode)
                    captureBuilder.set<Int>(CaptureRequest.SENSOR_SENSITIVITY, settings.lowerIsoValue)
                    captureBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, settings.exposureInMillis.toLong()*1000000)
                    captureBuilder.set<Long>(
                        CaptureRequest.SENSOR_EXPOSURE_TIME,
                        settings.exposureInMillis.toLong()*1000000
                    )


                    mImageReader = ImageReader.newInstance(
                        settings.width,
                        settings.height,
                        settings.imageFormat,
                        settings.maxImages
                    )


                    mImageReader!!.setOnImageAvailableListener(
                        this@Camera2PostConfigurationInterface,
                        getBackgroundHandler()
                    )


                    val surfaces = ArrayList<Surface>(1)
                    val readerSurface = mImageReader!!.surface
                    surfaces.add(readerSurface)
                    captureBuilder.addTarget(readerSurface)

                    cameraDevice.createCaptureSession(surfaces,
                        object : CameraCaptureSession.StateCallback() {

                            override fun onConfigured(session: CameraCaptureSession) {

                                startRepeatingRequest(session)

                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {

                                println("$TAG   onConfigureFailed")

                            }
                        }, getBackgroundHandler()
                    )


                }

                override fun onDisconnected(p0: CameraDevice) {

                    println("$TAG   onDisconnected")

                }

            }, getBackgroundHandler())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

    }


    override fun startRepeatingRequest(session: CameraCaptureSession) {
        this.session = session
        session.setRepeatingRequest(
            captureBuilder.build(),
            object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureStarted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    timestamp: Long,
                    frameNumber: Long
                ) {
                    println("==============capture starded ${TrueTimeRx.now().time - (timestamp / 1000)} ")
                    super.onCaptureStarted(session, request, timestamp, frameNumber)
                }

                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    println("==============capture completed  ${TrueTimeRx.now().time}    $result ")
                    super.onCaptureCompleted(session, request, result)
                }
            }, getBackgroundHandler()
        )
        lastFrameTimestamp = System.nanoTime()
    }

}