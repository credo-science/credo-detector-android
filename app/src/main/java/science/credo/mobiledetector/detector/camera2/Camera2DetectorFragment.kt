package science.credo.mobiledetector.detector.camera2

import android.graphics.ImageFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.*
import science.credo.mobiledetector.R
import science.credo.mobiledetector.detector.*
import science.credo.mobiledetector.detector.old.JniWrapper
import science.credo.mobiledetector.detector.old.OldCalibrationFinder
import science.credo.mobiledetector.detector.old.OldCalibrationResult
import science.credo.mobiledetector.detector.old.OldFrameAnalyzer
import science.credo.mobiledetector.settings.Camera2ApiSettings
import science.credo.mobiledetector.settings.ProcessingMethod
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.utils.UiUtils


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2DetectorFragment private constructor() :
    BaseDetectorFragment(R.layout.fragment_detector),
    CameraInterface.FrameCallback {

    companion object {
        fun instance(): Camera2DetectorFragment {
            return Camera2DetectorFragment()
        }

    }

    var settings: Camera2ApiSettings? = null
    var calibrationResult: BaseCalibrationResult? = null
    val oldCalibrationFinder: OldCalibrationFinder = OldCalibrationFinder()
    var rawCalibrationFinder: RawFormatCalibrationFinder? = null
    lateinit var frameAnalyzer: BaseFrameAnalyzer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)


        settings = Prefs.get(context!!, Camera2ApiSettings::class.java)

        infoDialogFragment = InfoDialogFragment.newInstance(settings!!)

        if (settings!!.imageFormat == ImageFormat.RAW_SENSOR &&
            settings!!.processingMethod == ProcessingMethod.EXPERIMENTAL
        ) {
            frameAnalyzer = RawFormatFrameAnalyzer
            rawCalibrationFinder = RawFormatCalibrationFinder(
                context!!,
                settings!!,
                object : RawFormatCalibrationFinder.CalibrationCallback {
                    override fun onStatusChanged(
                        state: State,
                        msg: String,
                        progress: Int,
                        avgNoise: Int
                    ) {
                        println("==========on status chaged $state  $msg   $progress")
                        updateState(state, "$msg - $progress%")
                    }

                    override fun onCalibrationSuccess(calibrationResult: RawFormatCalibrationResult) {

                        println("==========calibration found !! ${calibrationResult.detectionThreshold}  ${calibrationResult.calibrationNoise}")
                        this@Camera2DetectorFragment.calibrationResult = calibrationResult
                        infoDialogFragment?.setCalibrationResults(calibrationResult)
                        rawCalibrationFinder = null
                        start(settings!!)

                    }

                    override fun onCalibrationFailed() {
                        UiUtils.showAlertDialog(
                            context!!,
                            getString(R.string.detector_cant_calibrate_warning)
                        )
                    }

                }
            )
            rawCalibrationFinder!!.start()
        } else {
            frameAnalyzer = OldFrameAnalyzer
            start(settings!!)
        }

        return v
    }

    fun start(settings: Camera2ApiSettings) {
        startTimer()
        cameraInterface = Camera2PostConfigurationInterface(
            settings,
            this
        )
        cameraInterface?.start(context!!)
    }

//    fun mockDetection() {
//        GlobalScope.launch {
//            //        val dir = activity!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//            delay(5000)
//            val dir = "/storage/emulated/0/fake_detections/"
//            val file = File(dir + "2_hit_1576622145491kotlin.Unit")
//            val size = file.length().toInt()
//            val bytes = ByteArray(size)
//            try {
//                val buf = BufferedInputStream(FileInputStream(file))
//                buf.read(bytes, 0, bytes.size)
//                buf.close()
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            onFrameReceived(
//                (Frame(
//                    bytes,
//                    3968,
//                    2976,
//                    ImageFormat.RAW_SENSOR,
//                    700,
//                    System.currentTimeMillis() - 3000
//                ))
//            )
//        }
//
//    }

    override fun onFrameReceived(frame: Frame, sameFrameTimestamp: Long?) {
        GlobalScope.async {

            val ts = sameFrameTimestamp ?: TrueTimeRx.now().time


            if(JniWrapper.isBusy){
                return@async
            }
            val frameResult = when (settings!!.processingMethod) {
                ProcessingMethod.OFFICIAL -> {
                    JniWrapper.calculateFrame(
                        frame.byteArray,
                        frame.width,
                        frame.height,
                        (calibrationResult as OldCalibrationResult?)?.blackThreshold
                            ?: OldCalibrationResult.DEFAULT_BLACK_THRESHOLD
                    )
                }
                ProcessingMethod.EXPERIMENTAL -> {
                    JniWrapper.calculateFrame(
                        frame.byteArray,
                        frame.width,
                        frame.height,
                        (calibrationResult as RawFormatCalibrationResult).clusterFactorWidth,
                        (calibrationResult as RawFormatCalibrationResult).clusterFactorHeight,
                        if (settings!!.imageFormat == ImageFormat.RAW_SENSOR) 2 else 1
                    )
                }
            }
            infoDialogFragment?.setFrameResults(frameResult)

            if (frameResult.isCovered(calibrationResult)) {
                if (calibrationResult == null) {
                    calibrationResult =
                        oldCalibrationFinder.nextFrame(frameResult as OldFrameResult)
                    println("===$this====t2 = ${TrueTimeRx.now().time - ts}")
                    calibrationResult?.save(context!!)
                    infoDialogFragment?.setCalibrationResults(calibrationResult)
                    val progress =
                        (oldCalibrationFinder.counter.toFloat() / OldCalibrationFinder.CALIBRATION_LENGHT) * 100
                    updateState(State.CALIBRATION, "${String.format("%.2f", progress)}%")
                } else {
                    if (settings?.processingMethod == ProcessingMethod.EXPERIMENTAL) {
                        if ((frameResult as RawFormatFrameResult).avg > (calibrationResult as RawFormatCalibrationResult).calibrationNoise) {
                            //TODO recalibration here
                        }
                    }
                    val hit = frameAnalyzer.checkHit(
                        frame,
                        frameResult,
                        calibrationResult!!
                    )
                    println("===$this====t3 = ${TrueTimeRx.now().time - ts} $hit")
                    hit?.send(context!!)
                    hit?.saveToStorage(context!!)
                    if (hit != null) {
                        if(settings!!.processingMethod == ProcessingMethod.OFFICIAL){
                            onFrameReceived(frame,ts)
                        }
                    }
                    updateState(State.RUNNING, frame, hit)
                }
            } else {
                updateState(State.NOT_COVERED, frame)
            }
        }
    }


    fun updateState(state: State, additionalMsg: String) {
        super.updateState(state, null)
        GlobalScope.launch(Dispatchers.Main) {
            tvState?.text = "${tvState?.text.toString()}\n$additionalMsg"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rawCalibrationFinder?.stop()

    }

}