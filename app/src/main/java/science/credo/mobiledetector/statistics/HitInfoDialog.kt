package science.credo.mobiledetector.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
import science.credo.mobiledetector.utils.ConstantsNamesHelper
import java.lang.IllegalStateException
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
    var tvMax: TextView? = null
    var tvAverage: TextView? = null
    var tvBlacksPercentage: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_hit_info, container, false)

        tvBlacksPercentage = v.findViewById(R.id.tvBlacksPercentage)
        tvAverage = v.findViewById(R.id.tvAverage)
        tvMax = v.findViewById(R.id.tvMax)
        tvDate = v.findViewById(R.id.tvDate)

        if (hit.blacksPercentage != null) {
            tvBlacksPercentage?.text = "Blacks percentage: ${hit.blacksPercentage}"
        } else {
            tvBlacksPercentage?.visibility = View.GONE
        }
        tvAverage?.text = "Average brightness: ${hit.average}"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS")
        val d = Date()
        d.time = hit.timestamp ?: 0L
        val c = Calendar.getInstance()
        c.timeInMillis = hit.timestamp ?: 0L
        tvDate?.text =        sdf.format(d)
        tvMax?.text = "Max: ${hit.maxValue}"


        return v
    }


}