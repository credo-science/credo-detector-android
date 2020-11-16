package science.credo.mobiledetector2.detector

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import science.credo.mobiledetector2.R
import science.credo.mobiledetector2.analytics.CalibrationInabilityEvent
import science.credo.mobiledetector2.analytics.DetectorRunningEvent
import science.credo.mobiledetector2.settings.BaseSettings
import science.credo.mobiledetector2.settings.Camera2ApiSettings
import science.credo.mobiledetector2.settings.OldCameraSettings
import science.credo.mobiledetector2.settings.ProcessingMethod
import science.credo.mobiledetector2.utils.UiUtils
import java.lang.IllegalStateException

abstract class BaseDetectorFragment(private val layoutResource: Int) : Fragment() {

    var cameraInterface: CameraInterface? = null
    var ivProgress: ImageView? = null
    var tvState: TextView? = null
    var tvDetectionCount: TextView? = null
    var progressAnimation: AnimationDrawable? = null
    var tvRunningTime: TextView? = null
    var tvShowMoreInfo: TextView? = null
    lateinit var settings: BaseSettings
    var calibrationResult: BaseCalibrationResult? = null

    var timerJob: Job? = null
    var infoDialogFragment: InfoDialogFragment? = null
    var timeInSeconds = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(layoutResource, container, false)
        ivProgress = v.findViewById(R.id.ivProgress)
        tvState = v.findViewById(R.id.tvState)
        tvDetectionCount = v.findViewById(R.id.tvDetectionCount)
        ivProgress?.setBackgroundResource(R.drawable.anim_progress)
        tvRunningTime = v.findViewById(R.id.tvRunningTime)
        tvShowMoreInfo = v.findViewById(R.id.tvShowMoreInfo)

        ivProgress?.post {
            progressAnimation = ivProgress?.background as AnimationDrawable
        }

        tvShowMoreInfo?.setOnClickListener {
            GlobalScope.launch {
                infoDialogFragment?.show(
                    childFragmentManager,
                    infoDialogFragment!!::class.java.simpleName
                )
            }

        }


        return v

    }


    fun startTimer() {
        GlobalScope.launch(Dispatchers.Main) {
            tvRunningTime?.visibility = View.VISIBLE
        }
        timerJob = GlobalScope.launch {
            while (true) {
                delay(1000)
                timeInSeconds++
                val hours = timeInSeconds / 3600
                val minutes = (timeInSeconds / 60) % 60
                val seconds = timeInSeconds % 60
                if (timeInSeconds % 3600 == 0) {
                    sendDetectorRunningEvent()
                }
                if (hours == 0
                    && minutes == 5
                    && calibrationResult == null
                    && (settings is OldCameraSettings || (settings is Camera2ApiSettings && (settings as Camera2ApiSettings).processingMethod == ProcessingMethod.OFFICIAL))
                ) {
                    sendCalibrationInabilityEvent()
                }
                GlobalScope.launch(Dispatchers.Main) {
                    tvRunningTime?.text = "${String.format("%02d", hours)}:" +
                            "${String.format("%02d", minutes)}:" +
                            "${String.format("%02d", seconds)}"
                }
            }
        }
    }

    suspend fun sendDetectorRunningEvent() {
        DetectorRunningEvent(
            context!!,
            timeInSeconds,
            (tvDetectionCount?.tag as String?)?.toIntOrNull() ?: 0,
            settings,
            calibrationResult!!
        ).send()
    }

    suspend fun sendCalibrationInabilityEvent() {
        CalibrationInabilityEvent(
            context!!,
            settings
        ).send()
    }

    fun stopCamera() {
        cameraInterface?.stop()
        timerJob?.cancel()
    }


    fun updateState(state: State, frame: Frame?) {
        updateState(state, frame, null)
    }

    @SuppressLint("SetTextI18n")
    fun updateState(state: State, frame: Frame?, hit: Hit?) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                when (state) {
                    State.DISABLED -> {
                        tvState?.text = getString(R.string.detector_state_disabled)
                        tvState?.setTextColor(Color.RED)

                    }
                    State.NOT_COVERED -> {
                        tvState?.text = getString(R.string.detector_state_not_covered)
                        tvState?.setTextColor(Color.RED)
                        progressAnimation?.stop()

                    }
                    State.CALIBRATION -> {
                        tvState?.text = getString(R.string.detector_state_calibration)
                        progressAnimation?.start()
                        tvState?.setTextColor(Color.YELLOW)
                    }
                    State.RUNNING -> {
                        tvState?.text = getString(R.string.detector_state_running)
                        tvDetectionCount?.visibility = View.VISIBLE
                        if (hit != null) {
                            val counter =
                                ((tvDetectionCount?.tag as String?)?.toIntOrNull() ?: 0) + 1
                            tvDetectionCount?.text =
                                "Detections in this run : $counter\nLast detection at: ${UiUtils.timestampToReadableHour(
                                    hit.timestamp!!
                                )}"
                            tvDetectionCount?.tag = counter.toString()

                        }
                        tvState?.setTextColor(Color.GREEN)
                        progressAnimation?.start()

                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}