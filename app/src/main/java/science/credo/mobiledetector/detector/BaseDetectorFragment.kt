package science.credo.mobiledetector.detector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.*
import science.credo.mobiledetector.R
import science.credo.mobiledetector.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector.detector.camera2.RawFormatFrameResult
import science.credo.mobiledetector.detector.old.OldCalibrationResult
import science.credo.mobiledetector.settings.BaseSettings
import science.credo.mobiledetector.settings.Camera2ApiSettings
import science.credo.mobiledetector.settings.OldCameraSettings
import science.credo.mobiledetector.utils.ConstantsNamesHelper
import science.credo.mobiledetector.utils.UiUtils
import java.lang.IllegalStateException

abstract class BaseDetectorFragment(private val layoutResource: Int) : Fragment() {

    var cameraInterface: CameraInterface? = null
    var ivProgress: ImageView? = null
    var tvExposure: TextView? = null
    var tvFormat: TextView? = null
    var tvFrameSize: TextView? = null
    var tvState: TextView? = null
    var tvInterface: TextView? = null
    var tvDetectionCount: TextView? = null
    var progressAnimation: AnimationDrawable? = null
    var tvRunningTime: TextView? = null
    var tvCalibrationResult: TextView? = null
    var tvFrameResult: TextView? = null
    var timerJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(layoutResource, container, false)
        ivProgress = v.findViewById(R.id.ivProgress)
        tvFormat = v.findViewById(R.id.tvFormat)
        tvFrameSize = v.findViewById(R.id.tvFrameSize)
        tvState = v.findViewById(R.id.tvState)
        tvInterface = v.findViewById(R.id.tvInterface)
        tvDetectionCount = v.findViewById(R.id.tvDetectionCount)
        ivProgress?.setBackgroundResource(R.drawable.anim_progress)
        tvRunningTime = v.findViewById(R.id.tvRunningTime)
        tvCalibrationResult = v.findViewById(R.id.tvCalibrationResult)
        tvFrameResult = v.findViewById(R.id.tvFrameResult)
        tvExposure = v.findViewById(R.id.tvExposure)

        ivProgress?.post {
            progressAnimation = ivProgress?.background as AnimationDrawable
        }


        return v

    }


    fun startTimer() {
        var timeInSeconds = 0
        timerJob = GlobalScope.launch {
            while (true) {
                delay(1000)
                timeInSeconds++
                val hours = timeInSeconds / 3600
                val minutes = (timeInSeconds / 60) % 60
                val seconds = timeInSeconds % 60

                GlobalScope.launch(Dispatchers.Main) {
                    tvRunningTime?.text = "${String.format("%02d", hours)}:" +
                            "${String.format("%02d", minutes)}:" +
                            "${String.format("%02d", seconds)}"
                }
            }
        }
    }

    fun displayFrameSettings(settings: BaseSettings) {
        GlobalScope.launch(Dispatchers.Main) {
            tvFormat?.text =
                String.format(
                    "Format: %s",
                    ConstantsNamesHelper.getFormatName(settings.imageFormat)
                )
            tvFrameSize?.text =
                String.format("Frame size: %d x %d", settings.width, settings.height)
            if (settings is OldCameraSettings) {
                tvExposure?.text =
                    String.format("Fps range: %d-%d", settings.fpsRange[0], settings.fpsRange[1])
            } else if (settings is Camera2ApiSettings) {
                tvExposure?.text = String.format("Exposure time %d ms", settings.exposureInMillis)
            }
        }
    }

    fun displayCalibrationResults(calibrationResult: BaseCalibrationResult?) {
        if (calibrationResult == null) {
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            when (calibrationResult) {
                is RawFormatCalibrationResult -> {
                    tvCalibrationResult?.text =
                        String.format(
                            getString(R.string.detector_calibration_result_raw),
                            calibrationResult.clusterFactorWidth,
                            calibrationResult.clusterFactorHeight,
                            calibrationResult.detectionThreshold,
                            calibrationResult.calibrationNoise
                        )
                }
                is OldCalibrationResult -> {
                    tvCalibrationResult?.text =
                        String.format(
                            getString(R.string.detector_calibration_result_old),
                            calibrationResult.avg,
                            calibrationResult.blackThreshold,
                            calibrationResult.max
                        )
                }
                else -> {
                }
            }
        }
    }

    fun displayFrameResults(frameResult: BaseFrameResult) {
        GlobalScope.launch(Dispatchers.Main) {
            if (frameResult is RawFormatFrameResult) {
                tvFrameResult?.text =
                    String.format(
                        getString(R.string.detector_frame_result_raw),
                        frameResult.avg,
                        frameResult.max
                    )
            } else if (frameResult is OldFrameResult) {
                tvFrameResult?.text =
                    String.format(
                        getString(R.string.detector_frame_result_old),
                        frameResult.avg,
                        frameResult.blacksPercentage,
                        frameResult.max
                    )
            }
        }

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
                            val counter = ((tvDetectionCount?.tag as Int?) ?: 0) + 1
                            tvDetectionCount?.text =
                                "Detections in this run : $counter\nLast detection at: ${UiUtils.timestampToReadableHour(hit.timestamp!!)}"
                            tvDetectionCount?.tag = counter

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