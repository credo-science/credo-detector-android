package science.credo.mobiledetector.detector.old

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.settings.OldCameraSettings
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.detector.*


class OldDetectorFragment private constructor() : BaseDetectorFragment(R.layout.fragment_detector),
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
        val v = super.onCreateView(inflater, container, savedInstanceState)


        val settings = Prefs.get(context!!, OldCameraSettings::class.java)!!
        infoDialogFragment = InfoDialogFragment.newInstance(settings)

        cameraInterface = OldCameraInterface(
            this,
            settings
        )

        cameraInterface?.start(context!!)
        startTimer()
        return v
    }

    override fun onFrameReceived(frame: Frame, sameFrameTimestamp: Long?) {
        GlobalScope.async {
            val ts = sameFrameTimestamp ?: TrueTimeRx.now().time

            if (JniWrapper.isBusy) {
                println("===========status skipped")
                return@async
            }
            println("===========status running")

//            val frameResult = frameAnalyzer.baseCalculation(calibrationResult)
            val frameResult = JniWrapper.calculateFrame(
                frame.byteArray,
                frame.width,
                frame.height,
                calibrationResult?.blackThreshold ?: 40
            )
            infoDialogFragment?.setFrameResults(frameResult)

            if (frameResult.isCovered(calibrationResult)) {
                if (calibrationResult == null) {
                    calibrationResult = calibrationFinder.nextFrame(frameResult)
                    println("===$this====t2 = ${TrueTimeRx.now().time - ts}")
                    calibrationResult?.save(context!!)
                    infoDialogFragment?.setCalibrationResults(calibrationResult)
                    val progress =
                        (calibrationFinder.counter.toFloat() / OldCalibrationFinder.CALIBRATION_LENGHT) * 100
                    updateState(State.CALIBRATION, "${String.format("%.2f", progress)}%")

                } else {
                    val hit = OldFrameAnalyzer.checkHit(
                        frame,
                        frameResult,
                        calibrationResult!!
                    )
                    println("===$this====t3 = ${TrueTimeRx.now().time - ts} $hit")
                    hit?.send(context!!)
                    hit?.saveToStorage(context!!)
                    if (hit != null) {
                        onFrameReceived(frame, sameFrameTimestamp)
                    }
                    updateState(State.RUNNING, frame, hit)
                }
            } else {
                updateState(State.NOT_COVERED, frame)
            }
//

        }

    }

    fun updateState(state: State, additionalMsg: String) {
        super.updateState(state, null)
        GlobalScope.launch(Dispatchers.Main) {
            tvState?.text = "${tvState?.text.toString()}\n$additionalMsg"
        }
    }

}