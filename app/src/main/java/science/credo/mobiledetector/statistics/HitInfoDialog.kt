package science.credo.mobiledetector.statistics

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.detector.BaseCalibrationResult
import science.credo.mobiledetector.detector.BaseFrameResult
import science.credo.mobiledetector.detector.Hit
import science.credo.mobiledetector.detector.OldFrameResult
import science.credo.mobiledetector.detector.camera2.RawFormatCalibrationResult
import science.credo.mobiledetector.detector.camera2.RawFormatFrameResult
import science.credo.mobiledetector.detector.old.OldCalibrationResult
import science.credo.mobiledetector.settings.BaseSettings
import science.credo.mobiledetector.settings.Camera2ApiSettings
import science.credo.mobiledetector.settings.OldCameraSettings
import science.credo.mobiledetector.utils.BitmapUtils
import science.credo.mobiledetector.utils.ConstantsNamesHelper
import science.credo.mobiledetector.utils.UiUtils
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class HitInfoDialog private constructor() : DialogFragment() {

    companion object {
        fun newInstance(hit: Hit): HitInfoDialog {
            val instance = HitInfoDialog()
            instance.hit = hit
            return instance
        }
    }


    lateinit var hit: Hit

    var tvDate: TextView? = null
    var tvInfo: TextView? = null
    var tvProcessingMethod: TextView? = null
    var tvFormat: TextView? = null
    lateinit var ivHit: ImageView
    lateinit var btClose: View
    var scaledBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_hit_info, container, false)
        ivHit = v.findViewById(R.id.ivHit)
        tvInfo = v.findViewById(R.id.tvInfo)
        tvProcessingMethod = v.findViewById(R.id.tvProcessingMethod)
        tvFormat = v.findViewById(R.id.tvFormat)
        tvDate = v.findViewById(R.id.tvDate)
        btClose = v.findViewById(R.id.btClose)

        v.post {
            GlobalScope.launch {
                val bitmap = BitmapUtils.loadBitmap(hit.frameContent)
                if (bitmap != null) {
                    val factor = v.width / bitmap.width
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        bitmap.width * factor,
                        bitmap.height * factor,
                        false
                    )
                    bitmap.recycle()
                    afterBitmapCreation(scaledBitmap)
                }
            }
            println("==================== ${v.width}")
            println("==================== ${UiUtils.getScreenWidth()}")
        }
        return v
    }

    fun afterBitmapCreation(bitmap: Bitmap) {

        GlobalScope.launch(Dispatchers.Main) {
            ivHit.setImageBitmap(bitmap)
            val sb = StringBuilder()
            if (hit.processingMethod != null && hit.format != null) {
                tvProcessingMethod?.text = hit.processingMethod
                tvFormat?.text = hit.format
            } else {
                tvFormat?.visibility = View.GONE
                tvProcessingMethod?.visibility = View.GONE
            }

            if (hit.width != null && hit.height != null) {
                sb.append("\nSize: ${hit.width}x${hit.height}")
            }
            if (hit.exposure != null) {
                sb.append("\nExposure: ${hit.exposure}")
            }
            if (hit.maxValue != null) {
                sb.append("\nMax brightness : ${hit.maxValue}")
            }
            if (hit.average != null) {
                sb.append("\nAverage brightness: ${hit.average}")
            }
            if (hit.x != null && hit.y != null) {
                sb.append("\nHit coordinates: ${hit.x}x${hit.y}")
            }
            if (hit.threshold != null) {
                sb.append("\nThreshold: ${hit.threshold}")
            }
            if (hit.clusteringFactor != null) {
                sb.append("\nClustering factor: ${hit.clusteringFactor}")
            }
            if (hit.calibrationNoise != null) {
                sb.append("\nCalibration noise: ${hit.calibrationNoise}")
            }
            if (hit.thresholdAmplifier != null) {
                sb.append("\nThreshold amplifier: ${hit.thresholdAmplifier}")
            }
            if (hit.blacksPercentage != null) {
                sb.append("\nBlacks percentage: ${hit.blacksPercentage}")
            }
            if (hit.temperature != null) {
                sb.append("\nTemperature: ${hit.temperature}")
            }
            tvInfo?.text = sb
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            val d = Date()
            d.time = hit.timestamp ?: 0L
            val c = Calendar.getInstance()
            c.timeInMillis = hit.timestamp ?: 0L
            tvDate?.text = sdf.format(d)
            btClose.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        scaledBitmap?.recycle()
    }

}