package science.credo.mobiledetector.settings

import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.detector.old.OldCalibrationResult
import science.credo.mobiledetector.utils.ConstantsNamesHelper
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.utils.UiUtils
import java.lang.Exception

class OldApiSettingsFragment private constructor() : BaseSettingsFragment() {


    override suspend fun getSettings(): BaseSettings {

        val format = radioGroupPreviewFormat.findViewById<RadioButton>(
            radioGroupPreviewFormat.checkedRadioButtonId
        ).tag as Int

        val size = radioGroupFrameSize.findViewById<RadioButton>(
            radioGroupFrameSize.checkedRadioButtonId
        ).tag as Camera.Size

        val fpsRange = radioGroupFPSRange.findViewById<RadioButton>(
            radioGroupFPSRange.checkedRadioButtonId
        ).tag as IntArray

        return OldCameraSettings(
            format,
            size.width,
            size.height,
            fpsRange
        )
    }

    companion object {
        fun instance(): OldApiSettingsFragment {
            return OldApiSettingsFragment()
        }
    }

    lateinit var radioGroupPreviewFormat: RadioGroup
    lateinit var radioGroupFPSRange: RadioGroup
    lateinit var radioGroupFrameSize: RadioGroup

    lateinit var tvNoCalibrationWarning: TextView
    lateinit var tvCalibrationMax: TextView
    lateinit var tvCalibrationAvg: TextView
    lateinit var tvCalibrationBlackThreshold: TextView


    var camera: Camera? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_old_api_settings, container, false)
        radioGroupPreviewFormat = v.findViewById(R.id.radioGroupPreviewFormat)
        radioGroupFPSRange = v.findViewById(R.id.radioGroupFPSRange)
        radioGroupFrameSize = v.findViewById(R.id.radioGroupFrameSize)
        tvNoCalibrationWarning = v.findViewById(R.id.tvNoCalibrationWarning)
        tvCalibrationMax = v.findViewById(R.id.tvCalibrationMax)
        tvCalibrationAvg = v.findViewById(R.id.tvCalibrationAvg)
        tvCalibrationBlackThreshold = v.findViewById(R.id.tvCalibrationBlackThreshold)
        println("=======old ")

        GlobalScope.async {
            try {
                camera = Camera.open()
                camera!!.lock()
                afterCameraOpened()
            }catch (e : Exception){
                val alertDialog = UiUtils.showAlertDialog(
                    context!!,
                    "Cannot connect to the camera, make sure the camera is not being used by another application"
                )
                alertDialog.setOnDismissListener {
                    activity?.finish()
                }
            }
        }

        return v

    }

    private fun afterCameraOpened() {
        GlobalScope.launch(Dispatchers.Main) {

            val parameters: Camera.Parameters = camera!!.parameters;

            println("============ ${parameters.pictureFormat}")

            val currentSettings: OldCameraSettings? =
                Prefs.get(context!!, OldCameraSettings::class.java)

            setupPreviewFormatRG(parameters.supportedPreviewFormats, currentSettings?.imageFormat)
            setupFpsRG(parameters.supportedPreviewFpsRange, currentSettings?.fpsRange)
            setupFrameSizeRG(
                parameters.supportedPreviewSizes,
                currentSettings?.width,
                currentSettings?.height
            )

            val lastCalibration = Prefs.get(context!!, OldCalibrationResult::class.java)
            if (lastCalibration != null) {
                tvCalibrationBlackThreshold.visibility = View.VISIBLE
                tvCalibrationAvg.text =
                    String.format("Coverage (black) threshold: %d", lastCalibration.blackThreshold)
                tvCalibrationBlackThreshold.text =
                    String.format("Coverage (avg) threshold: %d", lastCalibration.avg)
                tvCalibrationMax.visibility = View.VISIBLE
                tvCalibrationMax.text =
                    String.format("Detection (max) threshold: %d", lastCalibration.max)
                tvNoCalibrationWarning.visibility = View.GONE
            } else {
                tvNoCalibrationWarning.visibility = View.VISIBLE
                tvCalibrationBlackThreshold.visibility = View.GONE
                tvCalibrationMax.visibility = View.GONE
            }
        }
    }

    private fun setupFpsRG(supportedPreviewFpsRange: List<IntArray>, currentFpsRange: IntArray?) {

        var currentChoiceIndex = 0
        for ((index, range) in supportedPreviewFpsRange.withIndex()) {
            val rb = UiUtils.createStyledRatioButton(context!!)
            rb.text = "(${range[0]},${range[1]})"
            rb.tag = range
            radioGroupFPSRange.addView(rb)
            if (currentFpsRange?.get(0) == range[0] &&
                currentFpsRange[1] == range[1]
            ) {
                currentChoiceIndex = index
            }
        }
        (radioGroupFPSRange.getChildAt(currentChoiceIndex) as RadioButton).isChecked = true

    }

    private fun setupPreviewFormatRG(supportedPreviewFormats: List<Int>, currentFormat: Int?) {

        var currentChoiceIndex = 0
        for ((index, format) in supportedPreviewFormats.withIndex()) {
            val rb = UiUtils.createStyledRatioButton(context!!)
            rb.tag = format
            rb.text = ConstantsNamesHelper.getFormatName(format)
            radioGroupPreviewFormat.addView(rb)
            if (currentFormat ?: ImageFormat.NV21 == format) {
                currentChoiceIndex = index
            }
        }
        (radioGroupPreviewFormat.getChildAt(currentChoiceIndex) as RadioButton).isChecked = true

    }

    private fun setupFrameSizeRG(
        supportedPreviewSizes: MutableList<Camera.Size>,
        currentWidth: Int?,
        currentHeight: Int?
    ) {

//        var currentChoiceIndex = supportedPreviewSizes.size/2
        var currentChoiceIndex = 0

        for ((index, size) in supportedPreviewSizes.withIndex()) {
            val rb = UiUtils.createStyledRatioButton(context!!)
            rb.text = "${size.width} x ${size.height}"
            rb.tag = size
            radioGroupFrameSize.addView(rb)
            if (currentWidth == size.width && currentHeight == size.height) {
                currentChoiceIndex = index
            }
        }
        (radioGroupFrameSize.getChildAt(currentChoiceIndex) as RadioButton).isChecked = true

    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.stopPreview()
        camera?.setPreviewCallback(null)
        camera?.release()
    }

}