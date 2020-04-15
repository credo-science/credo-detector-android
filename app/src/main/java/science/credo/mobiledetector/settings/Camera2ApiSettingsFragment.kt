package science.credo.mobiledetector.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import science.credo.mobiledetector.R
import science.credo.mobiledetector.utils.Prefs
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.widget.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import science.credo.mobiledetector.utils.ConstantsNamesHelper
import science.credo.mobiledetector.utils.UiUtils
import java.lang.IllegalArgumentException


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2ApiSettingsFragment : BaseSettingsFragment() {


    override suspend fun getSettings(): BaseSettings {

        val format = radioGroupImageFormat.findViewById<RadioButton>(
            radioGroupImageFormat.checkedRadioButtonId
        ).tag as Int

        val size = radioGroupFrameSize.findViewById<RadioButton>(
            radioGroupFrameSize.checkedRadioButtonId
        ).tag as Size

        val cameraId = radioGroupHardwareCamera.findViewById<RadioButton>(
            radioGroupHardwareCamera.checkedRadioButtonId
        ).tag as String

        val processingMethod = radioGroupProcessingMethod.findViewById<RadioButton>(
            radioGroupProcessingMethod.checkedRadioButtonId
        ).tag as ProcessingMethod

        val settings = Camera2ApiSettings(
            format,
            size.width,
            size.height,
            cameraId,
            tvSelectedExposure.text.split("ms")[0].toInt(),
            processingMethod
        )

        val template = testTemplate()

        settings.lowerIsoValue = lowerIsoValue
        settings.template = template
        settings.sendToServer = cbSendToServer.isChecked
        settings.saveToMemory = cbSaveToMemory.isChecked
        settings.saveFrameByteArraySource = cbSaveFullFrameArray.isChecked
        settings.thresholdMultiplier = tvSelectedThresholdMultiplier.text.toString().replace(",",".").toFloat()


        return settings

    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun instance(): Camera2ApiSettingsFragment {
            return Camera2ApiSettingsFragment()
        }
    }


    lateinit var radioGroupImageFormat: RadioGroup
    lateinit var tvMaxExposure: TextView
    lateinit var seekBarExposure: SeekBar
    lateinit var tvMinExposure: TextView
    lateinit var tvSelectedExposure: TextView
    lateinit var radioGroupHardwareCamera: RadioGroup
    lateinit var cameraManager: CameraManager
    lateinit var radioGroupFrameSize: RadioGroup
    lateinit var radioGroupProcessingMethod: RadioGroup
    lateinit var tvStallTime: TextView
    lateinit var cbSaveFullFrameArray: CheckBox
    lateinit var cbSaveToMemory: CheckBox
    lateinit var cbSendToServer: CheckBox

    lateinit var seekBarThresholdMultiplier: SeekBar
    lateinit var tvMaxThresholdMultiplier: TextView
    lateinit var tvMinThresholdMultiplier: TextView
    lateinit var tvSelectedThresholdMultiplier: TextView
    lateinit var containerThresholdMultiplier: LinearLayout

    var lowerIsoValue: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_camera2_api_settings, container, false)
        radioGroupImageFormat = v.findViewById(R.id.radioGroupImageFormat)
        tvSelectedExposure = v.findViewById(R.id.tvSelectedExposure)
        seekBarExposure = v.findViewById(R.id.seekBarExposure)
        tvMinExposure = v.findViewById(R.id.tvMinExposure)
        tvMaxExposure = v.findViewById(R.id.tvMaxExposure)
        cbSaveFullFrameArray = v.findViewById(R.id.cbSaveFullFrameArray)
        cbSendToServer = v.findViewById(R.id.cbSendToServer)
        cbSaveToMemory = v.findViewById(R.id.cbSaveToMemory)
        radioGroupHardwareCamera = v.findViewById(R.id.radioGroupHardwareCamera)
        radioGroupFrameSize = v.findViewById(R.id.radioGroupFrameSize)
        radioGroupProcessingMethod = v.findViewById(R.id.radioGroupProcessingMethod)
        tvStallTime = v.findViewById(R.id.tvStallTime)

        containerThresholdMultiplier = v.findViewById(R.id.containerThresholdMultiplier)
        tvSelectedThresholdMultiplier = v.findViewById(R.id.tvSelectedThresholdMultiplier)
        tvMinThresholdMultiplier = v.findViewById(R.id.tvMinThresholdMultiplier)
        tvMaxThresholdMultiplier = v.findViewById(R.id.tvMaxThresholdMultiplier)
        seekBarThresholdMultiplier = v.findViewById(R.id.seekBarThresholdMultiplier)

        val currentSettings: Camera2ApiSettings? =
            Prefs.get(context!!, Camera2ApiSettings::class.java)

        setupProcessingMethod(currentSettings?.processingMethod)

        tvMinThresholdMultiplier.text = "1.05"
        tvMaxThresholdMultiplier.text = "2.50"
        tvSelectedThresholdMultiplier.text =
            (currentSettings?.thresholdMultiplier ?: 1.05).toString()
        seekBarThresholdMultiplier.max = 19
        seekBarThresholdMultiplier.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = if (progress < 10) {
                    (progress+1) * 0.05f
                } else {
                    10 * 0.05f + (progress-9) * 0.10f
                } + 1
                tvSelectedThresholdMultiplier.text = String.format("%.2f",value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })


        cbSaveFullFrameArray.isChecked = currentSettings?.saveFrameByteArraySource ?: false
        cbSaveToMemory.isChecked = currentSettings?.saveToMemory ?: false
        cbSendToServer.isChecked = currentSettings?.sendToServer ?: true

        setupHardwareCameraRG(currentSettings)


        return v

    }

    private fun setupProcessingMethod(currentProcessingMethod: ProcessingMethod?) {

        var selection = 0
        for ((index, processingMethod) in ProcessingMethod.values().withIndex()) {
            val rb = UiUtils.createStyledRatioButton(context!!)
            rb.text = processingMethod.name
            rb.tag = processingMethod
            if (processingMethod == currentProcessingMethod) {
                selection = index
            }
            radioGroupProcessingMethod.addView(rb)
        }
        radioGroupProcessingMethod.setOnCheckedChangeListener { group, checkedId ->
            if (group.findViewById<RadioButton>(checkedId).tag == ProcessingMethod.EXPERIMENTAL) {
                containerThresholdMultiplier.visibility = View.VISIBLE
            } else {
                containerThresholdMultiplier.visibility = View.GONE
            }
        }

        (radioGroupProcessingMethod.getChildAt(selection) as RadioButton).isChecked = true
        (radioGroupProcessingMethod.getChildAt(1) as RadioButton).isEnabled = false


    }

    private fun setupHardwareCameraRG(currentSettings: Camera2ApiSettings?) {

        var currentSelection = 0
        for ((index, cameraId) in cameraManager.cameraIdList.withIndex()) {
            if (currentSettings?.cameraId == cameraId) {
                currentSelection = index
            }
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)

            lowerIsoValue =
                cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.lower
                    ?: 0

            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            val radioButton = UiUtils.createStyledRatioButton(context!!)
            radioButton.text = when (facing) {
                CameraCharacteristics.LENS_FACING_FRONT ->
                    getString(R.string.settings_camera_front)
                CameraCharacteristics.LENS_FACING_BACK ->
                    getString(R.string.settings_camera_back)
                CameraCharacteristics.LENS_FACING_EXTERNAL ->
                    getString(R.string.settings_camera_external)
                else -> "Unknown (id:$cameraId)"
            }
            radioButton.tag = cameraId
            radioGroupHardwareCamera.addView(radioButton)
        }

        var first = true
        radioGroupHardwareCamera.setOnCheckedChangeListener { group, checkedId ->
            val cameraId = group.findViewById<RadioButton>(checkedId).tag as String
            setupImageFormat(
                cameraId,
                if (first) currentSettings else null
            )
            first = false
        }

        (radioGroupHardwareCamera.getChildAt(currentSelection) as RadioButton).isChecked = true
    }

    private fun setupImageFormat(cameraId: String, currentSettings: Camera2ApiSettings?) {

        val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
        val scalerMap = cameraCharacteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )
        radioGroupImageFormat.removeAllViews()
        var currentSelection = 0
        var index = 0
        for (imageFormat in scalerMap!!.outputFormats) {
            val radioButton = UiUtils.createStyledRatioButton(context!!)
            val formatName = ConstantsNamesHelper.getFormatName(imageFormat) ?: continue
            if (currentSettings?.imageFormat == imageFormat) {
                currentSelection = index
            }
            index++
            radioButton.text = formatName
            radioButton.tag = imageFormat
            radioGroupImageFormat.addView(radioButton)
        }
        radioGroupImageFormat.setOnCheckedChangeListener { group, checkedId ->
            val imageFormat = group.findViewById<RadioButton>(checkedId).tag as Int
            setupFrameSizeRG(imageFormat, scalerMap, currentSettings)
            (radioGroupProcessingMethod.getChildAt(1) as RadioButton).isEnabled =
                imageFormat == ImageFormat.RAW_SENSOR
        }
        (radioGroupImageFormat.getChildAt(currentSelection) as RadioButton).isChecked = true
    }

    private fun setupFrameSizeRG(
        imageFormat: Int,
        scalerMap: StreamConfigurationMap,
        currentSettings: Camera2ApiSettings?
    ) {

        radioGroupFrameSize.removeAllViews()
        var currentSelection = 0
        for ((index, size) in scalerMap.getOutputSizes(imageFormat).withIndex()) {
            if (size.width == currentSettings?.width && size.height == currentSettings.height) {
                currentSelection = index
            }
            val radioButton = UiUtils.createStyledRatioButton(context!!)
            radioButton.text = "$size"
            radioButton.tag = size
            radioGroupFrameSize.addView(radioButton)
        }
        radioGroupFrameSize.setOnCheckedChangeListener { group, checkedId ->
            val size = group.findViewById<RadioButton>(checkedId).tag as Size
            setupExposureSeekBar(imageFormat, size, scalerMap, currentSettings)
        }
        (radioGroupFrameSize.getChildAt(currentSelection) as RadioButton).isChecked = true
    }

    private fun setupExposureSeekBar(
        imageFormat: Int,
        size: Size,
        scalerMap: StreamConfigurationMap,
        currentSettings: Camera2ApiSettings?
    ) {

        val currentProgress = (currentSettings?.exposureInMillis ?: 0) / 100

        println("======current progress $currentProgress")

        val minFrameDuration = scalerMap.getOutputMinFrameDuration(imageFormat, size)
        val minInMillis = (minFrameDuration / 1000000)
        tvMinExposure.text = "${minInMillis}ms"
        tvMaxExposure.text = "2000ms"
        seekBarExposure.max = 20
        seekBarExposure.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 0) {
                    tvSelectedExposure.text = "${minInMillis}ms"
                } else {
                    tvSelectedExposure.text = "${(100 * progress)}ms"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        tvSelectedExposure.text = "${minInMillis}ms"
        seekBarExposure.progress = currentProgress

        tvStallTime.text = String.format(
            "Stall time: %dms",
            (scalerMap.getOutputStallDuration(imageFormat, size) / 1000000)
        )


    }

    @SuppressLint("MissingPermission")
    private suspend fun testTemplate(): Int {

        val mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread.start()
        val mBackgroundHandler = Handler(mBackgroundThread.looper)

        var job: Job? = null

        var template = CameraDevice.TEMPLATE_MANUAL

        val cameraCallback = object : CameraDevice.StateCallback() {
            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {
                template = CameraDevice.TEMPLATE_PREVIEW
                job?.cancel()
            }

            override fun onOpened(p0: CameraDevice) {
                try {
                    p0.createCaptureRequest(template)
                } catch (ex: IllegalArgumentException) {
                    template = CameraDevice.TEMPLATE_PREVIEW
                } finally {
                    job?.cancel()
                    p0.close()
                }
            }
        }

        job = GlobalScope.launch {

            val cameraId = radioGroupHardwareCamera.findViewById<RadioButton>(
                radioGroupHardwareCamera.checkedRadioButtonId
            ).tag as String
            val cameraManager: CameraManager =
                context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager.openCamera(cameraId, cameraCallback, mBackgroundHandler)
            while (true) {
                delay(1000)
            }

        }
        job.join()
        return template
    }


}