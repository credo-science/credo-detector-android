package science.credo.mobiledetector.fragment

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import science.credo.mobiledetector.databinding.FragmentStatusBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import science.credo.mobiledetector.BlankActivity
import science.credo.mobiledetector.CredoApplication

import science.credo.mobiledetector.R
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.database.DataManager
import science.credo.mobiledetector.database.UserInfoWrapper
import science.credo.mobiledetector.detection.CameraPreviewCallbackNative
import science.credo.mobiledetector.events.BatteryEvent
import science.credo.mobiledetector.events.DetectorStateEvent
import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.events.UiUpdateEvent
import science.credo.mobiledetector.info.*
import java.text.SimpleDateFormat
import java.util.*


class StatusFragment : Fragment() {
    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private val DETECTION_OFF_LABEL = 0
    private val DETECTION_ON_LABEL = 1
    private val DETECTION_HOLD_LABEL = 2

    private val DETECTOR_BUTTON_ON = 0
    private val DETECTOR_BUTTON_OFF = 1

    private val mReceiver = PowerConnectionReceiver()
    private var detectorState: DetectorStateEvent = DetectorStateEvent()
    private var statsEvent = StatsEvent()
    private var batteryState = BatteryEvent()

    private var mListener: OnFragmentInteractionListener? = null

    private fun detectionStateLabel() : Int {
        return when (detectorState.running) {
            false -> DETECTION_OFF_LABEL
            true -> when (statsEvent.activeDetection && detectorState.cameraOn) {
                true -> DETECTION_ON_LABEL
                false -> DETECTION_HOLD_LABEL
            }
        }
    }

    private fun detectionButton() : Int {
        return when (detectorState.running) {
            false -> DETECTOR_BUTTON_ON
            true -> DETECTOR_BUTTON_OFF
        }
    }

    private fun showStateLabel(nr: Int) {
        when(nr) {
            DETECTION_OFF_LABEL -> {
                binding.detectionOffLabel.visibility = View.VISIBLE
                binding.detectionOnLabel.visibility = View.GONE
                binding.detectionHoldLabel.visibility = View.GONE
            }

            DETECTION_ON_LABEL -> {
                binding.detectionOffLabel.visibility = View.GONE
                binding.detectionOnLabel.visibility = View.VISIBLE
                binding.detectionHoldLabel.visibility = View.GONE
            }

            DETECTION_HOLD_LABEL -> {
                binding.detectionOffLabel.visibility = View.GONE
                binding.detectionOnLabel.visibility = View.GONE
                binding.detectionHoldLabel.visibility = View.VISIBLE
            }
        }
    }

    private fun showDetectionButton(nr: Int) {
        when(nr) {
            DETECTOR_BUTTON_ON -> {
                binding.startButton.visibility = View.VISIBLE
                binding.stopButton.visibility = View.GONE
            }

            DETECTOR_BUTTON_OFF -> {
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE
            }
        }
    }

    private fun fillInValuesOnPage() {
        val ui = UserInfoWrapper(context!!)
        val cw = ConfigurationWrapper(context!!)

        binding.nameText.text = ui.displayName
        binding.emailText.text = ui.email
        binding.teamText.text = ui.team

        showStateLabel(detectionStateLabel())
        showDetectionButton(detectionButton())
        binding.coverageLabel.visibility = if (!statsEvent.activeDetection && detectorState.running) View.VISIBLE else View.GONE
        binding.blankButton.visibility = if (statsEvent.activeDetection && detectorState.running) View.VISIBLE else View.GONE
        if (cw.localizationNeedUpdate > 0) {
            binding.updateLocationLabel.visibility = View.VISIBLE
            binding.updateLocationLabelDescription.visibility = View.VISIBLE
        } else {
            binding.updateLocationLabel.visibility = View.GONE
            binding.updateLocationLabelDescription.visibility = View.GONE
        }

        binding.detectionsLabel.text = getString(R.string.status_fragment_detections, DataManager.TRIM_PERIOD_HITS_DAYS)

        if (binding.detectionStats.visibility == View.GONE && detectorState.running) {
            binding.showStatistic.visibility = View.VISIBLE
        }

        binding.workingText.text = timePeriodFormat(statsEvent.onTime, false)

        val dm = DataManager.getDefault(context!!)
        binding.detectionsText.text = dm.getHitsCount().toString()
        dm.closeDb()

        val pm = PreferenceManager.getDefaultSharedPreferences(context)
        binding.pointsInfo.text = pm.getInt("points_pm", 0).toString()

        binding.startText.text = if (statsEvent.startDetectionTimestamp > 0)
            dateFormat.format(statsEvent.startDetectionTimestamp)
        else
            ""

        binding.latestText.text = if (statsEvent.lastFrameAchievedTimestamp > 0)
            dateFormat.format(statsEvent.lastFrameAchievedTimestamp)
        else
            ""

        binding.hitText.text = if (statsEvent.lastHitTimestamp > 0)
            dateFormat.format(statsEvent.lastHitTimestamp)
        else
            ""

        binding.coordinatesText.text = "%.4f %.4f".format(cw.localizationLongitude, cw.localizationLatitude)
        binding.temperatureLoginInfo.text = "%d°C".format(detectorState.temperature)

        binding.frameSizeText.text = "${statsEvent.frameWidth}x${statsEvent.frameHeight}"

        binding.frameCountText.text = statsEvent.allFrames.toString()
        binding.framePerformedText.text = statsEvent.performedFrames.toString()

        binding.ontimeText.text = timePeriodFormat(statsEvent.onTime, false)
        binding.blackText.text = "%.0f‰".format(statsEvent.blacksStats.average)

        binding.brightText.text = "%.2f".format(statsEvent.averageStats.average)
        binding.maxText.text = statsEvent.maxStats.max.toString()

        if (detectorState.type > DetectorStateEvent.StateType.Normal) {
            binding.errorText.visibility = View.VISIBLE
            binding.errorText.text = detectorState.status
        } else {
            binding.errorText.visibility = View.GONE
        }

        when (batteryState.isCharging) {
            true -> when(batteryState.acCharge) {
                true -> binding.batteryText.text = getString(R.string.status_fragment_battery_charge)
                false -> binding.batteryText.text = getString(R.string.status_fragment_battery_slow)
            }
            false -> binding.batteryText.text = getString(R.string.status_fragment_battery_nocharge)
        }

        binding.levelText.text = "%d%%".format(batteryState.batteryPct)
        binding.orientationText.text = "%.2f°".format(detectorState.orientation)
        binding.accText.text = "X:%.1f Y:%.1f Z:%.1f".format(detectorState.accX, detectorState.accY, detectorState.accZ)

        binding.benchmarkText.text = "%dms".format(CameraPreviewCallbackNative.benchmark)
        binding.fpsText.text = "%d".format(CameraPreviewCallbackNative.fps)
        binding.temperature.text = "%d".format(detectorState.temperature)
        binding.cropSize.text = "%d".format(ConfigurationInfo(requireContext()).cropSize)
    }

    override fun onResume() {

//        Log.d(TAG,"onResume")
        fillInValuesOnPage()
        //defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        val batteryIntent = context!!.registerReceiver(mReceiver, filter)
        if (batteryIntent != null) {
            batteryState = PowerConnectionReceiver.parseIntent(batteryIntent)
        }
        super.onResume()
    }

    override fun onPause() {

        //defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        context!!.unregisterReceiver(mReceiver)
        super.onPause()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG,"onCreateView")
        detectorState = (context!!.applicationContext as CredoApplication).detectorState
        EventBus.getDefault().register(this)
        _binding = FragmentStatusBinding.inflate(inflater, container, false)
        val v = binding.root

        binding.startButton.setOnClickListener {
            mListener!!.onStartDetection()
            //binding.showStatistic.visibility = View.VISIBLE
        }

        binding.stopButton.setOnClickListener {
            mListener!!.onStopDetection()
        }

        binding.blankButton.setOnClickListener {
            startActivity(Intent(context, BlankActivity::class.java))
        }

        binding.showStatistic.setOnClickListener{
            if(binding.detectionStats.visibility == View.GONE){
                binding.detectionStats.visibility = View.VISIBLE
                binding.showStatistic.setText(R.string.status_fragment_hide_statistic)
            }
            else{
                binding.detectionStats.visibility = View.GONE
                binding.showStatistic.setText(R.string.status_fragment_show_statistic)
            }
        }
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG,"onAttach")
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        EventBus.getDefault().unregister(this)
        super.onDetach()
        Log.d(TAG,"onDetach")
        mListener = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDetectorStateChange(detectorStateEvent: DetectorStateEvent) {
        detectorState = detectorStateEvent
        fillInValuesOnPage()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrameEvent(statsEvent: StatsEvent) {
        this.statsEvent = statsEvent
        fillInValuesOnPage()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBatteryEvent(batteryEvent: BatteryEvent) {
        if (batteryEvent.temperature != 0 || batteryState.temperature == 0) {
            batteryState = batteryEvent
        } else {
            // Keep previous temperature if new one is 0 (likely from power event without battery extras)
            val oldTemp = batteryState.temperature
            batteryState = batteryEvent
            batteryState.temperature = oldTemp
        }
        fillInValuesOnPage()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateUi(event: UiUpdateEvent) {
        fillInValuesOnPage()
    }

    private fun count_points(){
        val pm = PreferenceManager.getDefaultSharedPreferences(context)
        var score = pm.getInt("points_pm", 0)
        score += statsEvent.allFrames * 10
        score += statsEvent.onTime.toInt() * 1
        pm.edit().putInt("points_pm", score).commit()
    }


    interface OnFragmentInteractionListener {
        fun onStartDetection()
        fun onStopDetection()
    }

    companion object {

        val TAG = "StatusFragment"
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        fun newInstance(): StatusFragment {
            val fragment = StatusFragment()
            return fragment
        }

        fun timePeriodFormat(period : Long, microSeconds : Boolean): String {
            val hours = period / (60L * 60L * 1000L)
            val minutes = (period % (60L * 60L * 1000L)) / (60L * 1000L)
            val seconds = (period % (60L * 1000L)) / (1000L)
            val ms = period % 1000L

            if (microSeconds) {
                return "%d:%02d:%02d.%03d".format(hours, minutes, seconds, ms)
            } else {
                return "%d:%02d:%02d".format(hours, minutes, seconds)
            }
        }
    }
}
