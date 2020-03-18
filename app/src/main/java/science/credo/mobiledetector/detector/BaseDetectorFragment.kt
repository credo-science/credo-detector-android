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
import science.credo.mobiledetector.utils.ConstantsNamesHelper
import java.lang.IllegalStateException

abstract class BaseDetectorFragment : Fragment() {

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

    var timerJob: Job? = null


    fun startTimer() {
        var timeInSeconds = 0
        timerJob = GlobalScope.launch {
            while (true){
                delay(1000)
                timeInSeconds++
                val hours = timeInSeconds / 3600
                val minutes = timeInSeconds / 60
                val seconds = timeInSeconds % 60

                GlobalScope.launch(Dispatchers.Main) {
                    tvRunningTime?.text = "${String.format("%02d", hours)}:" +
                            "${String.format("%02d", minutes)}:" +
                            "${String.format("%02d", seconds)}"
                }
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
                if (frame != null) {
                    tvFormat?.text =
                        String.format(
                            "Format: %s",
                            ConstantsNamesHelper.getFormatName(frame.imageFormat)
                        )
                    tvFrameSize?.text =
                        String.format("Frame size: %d x %d", frame.width, frame.height)
                    tvExposure?.text = String.format("Exposure time %d ms", frame.exposureTime)
                }

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
                            tvDetectionCount?.text =
                                "Detections in this run : ${tvDetectionCount?.tag}\nLast detection ${(TrueTimeRx.now().time - hit.timestamp!!) / 1000f / 60f} minutes ago"
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