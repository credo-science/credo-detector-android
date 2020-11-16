package science.credo.mobiledetector.detector

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector.detector.camera2.RawFormatFrameResult
import science.credo.mobiledetector.detector.old.OldCalibrationResult
import science.credo.mobiledetector.settings.BaseSettings
import science.credo.mobiledetector.settings.Camera2ApiSettings
import science.credo.mobiledetector.settings.OldCameraSettings
import science.credo.mobiledetector.utils.ConstantsNamesHelper
import java.lang.IllegalStateException

class InfoDialogFragment private constructor() : DialogFragment() {

    companion object {
        fun newInstance(settings: BaseSettings): InfoDialogFragment {
            val instance = InfoDialogFragment()
            instance.settings = settings
            return instance
        }
    }


    lateinit var settings: BaseSettings

    var calibrationResult: BaseCalibrationResult? = null
    var frameResult: BaseFrameResult? = null

    var tvExposure: TextView? = null
    var tvFrameResult: TextView? = null
    var tvCalibrationResult: TextView? = null
    var tvInterface: TextView? = null
    var tvFrameSize: TextView? = null
    var tvFormat: TextView? = null
    var btClose: TextView? = null

    var isDisplayed = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE);
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(activity!!.resources.getColor(R.color.colorPrimary)));
//        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//
//        dialog?.window?.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP);
//        val p = dialog?.window?.attributes;
//        p?.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        p?.softInputMode = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;
//        p?.x = 200;
//        dialog?.window?.attributes = p;

        val v = inflater.inflate(R.layout.fragment_dialog_detector_info, container, false)


        tvExposure = v.findViewById(R.id.tvExposure)
        tvFrameResult = v.findViewById(R.id.tvFrameResult)
        tvCalibrationResult = v.findViewById(R.id.tvCalibrationResult)
        tvInterface = v.findViewById(R.id.tvInterface)
        tvFrameSize = v.findViewById(R.id.tvFrameSize)
        tvFormat = v.findViewById(R.id.tvFormat)
        btClose = v.findViewById(R.id.btClose)

        displayFrameSettings(settings)

        if (settings is OldCameraSettings) {
            tvInterface?.text = "Camera interface: Old"
        } else if (settings is Camera2ApiSettings) {
            tvInterface?.text = "Camera interface: Camera2\n" +
                    "Processing method: ${(settings as Camera2ApiSettings).processingMethod}"
        }


        btClose?.setOnClickListener {
            dismissAllowingStateLoss()
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }



    override fun onStart() {
        super.onStart()
        isDisplayed = true

        displayCalibrationResults(calibrationResult)
        displayFrameResults(frameResult)
    }

    override fun onStop() {
        super.onStop()

        isDisplayed = false
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

    fun setCalibrationResults(calibrationResult: BaseCalibrationResult?) {
        this.calibrationResult = calibrationResult
        if (isDisplayed) {
            displayCalibrationResults(calibrationResult)
        }
    }

    fun setFrameResults(frameResult: BaseFrameResult) {
        this.frameResult = frameResult
        if (isDisplayed) {
            displayFrameResults(frameResult)

        }
    }

    private fun displayCalibrationResults(calibrationResult: BaseCalibrationResult?) {
        try {
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
                                calibrationResult.max,
                                calibrationResult.avgAvg,
                                calibrationResult.avgMax,
                                calibrationResult.avgBlacksPercentage
                            )
                    }
                    else -> {
                    }
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }


    fun displayFrameResults(frameResult: BaseFrameResult?) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
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
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}