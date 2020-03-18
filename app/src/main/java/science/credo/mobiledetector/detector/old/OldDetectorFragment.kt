package science.credo.mobiledetector.detector.old

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import science.credo.mobiledetector.R
import science.credo.mobiledetector.settings.OldCameraSettings
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.detector.*


class OldDetectorFragment private constructor() : BaseDetectorFragment(),
    CameraInterface.FrameCallback {


    companion object {
        fun instance(): OldDetectorFragment {
            return OldDetectorFragment()
        }
    }

    var calibrationResult: OldCalibrationResult? = null
    val calibrationFinder: OldCalibrationFinder = OldCalibrationFinder()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_detector, container, false)

        ivProgress = v.findViewById(R.id.ivProgress)
        tvExposure = v.findViewById(R.id.tvExposure)
        tvFormat = v.findViewById(R.id.tvFormat)
        tvFrameSize = v.findViewById(R.id.tvFrameSize)
        tvInterface = v.findViewById(R.id.tvInterface)
        tvState = v.findViewById(R.id.tvState)
        tvDetectionCount = v.findViewById(R.id.tvDetectionCount)
        ivProgress?.setBackgroundResource(R.drawable.anim_progress)
        tvRunningTime = v.findViewById(R.id.tvRunningTime)
        ivProgress?.post {
            progressAnimation = ivProgress?.background as AnimationDrawable
        }


        tvInterface?.text = "Camera interface: Old"


        cameraInterface = OldCameraInterface(
            this,
            Prefs.get(context!!, OldCameraSettings::class.java)!!
        )
        cameraInterface?.start(context!!)
        startTimer()
        return v
    }


    override fun onFrameReceived(frame: Frame) {
        GlobalScope.async {
            val ts = TrueTimeRx.now().time


//            val frameResult = frameAnalyzer.baseCalculation(calibrationResult)
            val frameResult = JniWrapper.calculateFrame(
                frame.byteArray,
                frame.width,
                frame.height,
                calibrationResult?.blackThreshold ?: 40
            )

            if (frameResult.isCovered(calibrationResult)) {
                if (calibrationResult == null) {
                    calibrationResult = calibrationFinder.nextFrame(frameResult)
                    println("===$this====t2 = ${TrueTimeRx.now().time - ts}")
                    calibrationResult?.save(context!!)
                    updateState(State.CALIBRATION, frame)
                } else {
                    val hit = OldFrameAnalyzer.checkHit(
                        frame,
                        frameResult,
                        calibrationResult!!
                    )
                    println("===$this====t3 = ${TrueTimeRx.now().time - ts} $hit")
                    hit?.send(context!!)
                    updateState(State.RUNNING, frame, hit)
                }
            } else {
                updateState(State.NOT_COVERED, frame)
            }
//

        }

    }

}