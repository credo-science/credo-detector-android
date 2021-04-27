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
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.android.synthetic.main.fragment_status.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import science.credo.mobiledetector.BlankActivity
import science.credo.mobiledetector.CredoApplication

import science.credo.mobiledetector.R
import science.credo.mobiledetector.database.DataManager
import science.credo.mobiledetector.database.UserInfoWrapper
import science.credo.mobiledetector.detection.CameraPreviewCallbackNative
import science.credo.mobiledetector.events.BatteryEvent
import science.credo.mobiledetector.events.DetectorStateEvent
import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.info.*
import java.text.SimpleDateFormat
import java.util.*


class StatusFragment : Fragment() {

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
                detection_off_label.visibility = View.VISIBLE
                detection_on_label.visibility = View.GONE
                detection_hold_label.visibility = View.GONE
            }

            DETECTION_ON_LABEL -> {
                detection_off_label.visibility = View.GONE
                detection_on_label.visibility = View.VISIBLE
                detection_hold_label.visibility = View.GONE
            }

            DETECTION_HOLD_LABEL -> {
                detection_off_label.visibility = View.GONE
                detection_on_label.visibility = View.GONE
                detection_hold_label.visibility = View.VISIBLE
            }
        }
    }

    private fun showDetectionButton(nr: Int) {
        when(nr) {
            DETECTOR_BUTTON_ON -> {
                start_button.visibility = View.VISIBLE
                stop_button.visibility = View.GONE
            }

            DETECTOR_BUTTON_OFF -> {
                start_button.visibility = View.GONE
                stop_button.visibility = View.VISIBLE
            }
        }
    }

    private fun fillInValuesOnPage() {
        val ui = UserInfoWrapper(context!!)

        name_text.text = ui.displayName
        email_text.text = ui.email
        team_text.text = ui.team

        showStateLabel(detectionStateLabel())
        showDetectionButton(detectionButton())
        coverage_label.visibility = if (!statsEvent.activeDetection && detectorState.running) View.VISIBLE else View.GONE
        blank_button.visibility = if (statsEvent.activeDetection && detectorState.running) View.VISIBLE else View.GONE

        detections_label.text = getString(R.string.status_fragment_detections, DataManager.TRIM_PERIOD_HITS_DAYS)

        if (detection_stats.visibility == View.GONE && detectorState.running) {
            show_statistic.visibility = View.VISIBLE
        }

        working_text.text = timePeriodFormat(statsEvent.onTime, false)

        val dm = DataManager.getDefault(context!!)
        detections_text.text = dm.getHitsCount().toString()
        dm.closeDb()

        val pm = PreferenceManager.getDefaultSharedPreferences(context)
        points_info.text = pm.getInt("points_pm", 0).toString()

        start_text.text = if (statsEvent.startDetectionTimestamp > 0)
            dateFormat.format(statsEvent.startDetectionTimestamp)
        else
            ""

        latest_text.text = if (statsEvent.lastFrameAchievedTimestamp > 0)
            dateFormat.format(statsEvent.lastFrameAchievedTimestamp)
        else
            ""

        hit_text.text = if (statsEvent.lastHitTimestamp > 0)
            dateFormat.format(statsEvent.lastHitTimestamp)
        else
            ""

        frame_size_text.text = "${statsEvent.frameWidth}x${statsEvent.frameHeight}"

        frame_count_text.text = statsEvent.allFrames.toString()
        frame_performed_text.text = statsEvent.performedFrames.toString()

        ontime_text.text = timePeriodFormat(statsEvent.onTime, false)
        black_text.text = "%.0f‰".format(statsEvent.blacksStats.average)

        bright_text.text = "%.2f".format(statsEvent.averageStats.average)
        max_text.text = statsEvent.maxStats.max.toString()

        if (detectorState.type > DetectorStateEvent.StateType.Normal) {
            error_text.visibility = View.VISIBLE
            error_text.text = detectorState.status
        } else {
            error_text.visibility = View.GONE
        }

        when (batteryState.isCharging) {
            true -> when(batteryState.acCharge) {
                true -> battery_text.text = getString(R.string.status_fragment_battery_charge)
                false -> battery_text.text = getString(R.string.status_fragment_battery_slow)
            }
            false -> battery_text.text = getString(R.string.status_fragment_battery_nocharge)
        }

        level_text.text = "%d%%".format(batteryState.batteryPct)
        orientation_text.text = "%.2f°".format(detectorState.orientation)
        acc_text.text = "X:%.1f Y:%.1f Z:%.1f".format(detectorState.accX, detectorState.accY, detectorState.accZ)

        benchmark_text.text = "%dms".format(CameraPreviewCallbackNative.benchmark)
        fps_text.text = "%d".format(CameraPreviewCallbackNative.fps)
    }

    override fun onResume() {

//        Log.d(TAG,"onResume")
        fillInValuesOnPage()
        //defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        context!!.registerReceiver(mReceiver, filter)
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
        val v = inflater.inflate(R.layout.fragment_status, container, false)

        v.start_button.setOnClickListener {
            mListener!!.onStartDetection()
            //show_statistic.visibility = View.VISIBLE
        }

        v.stop_button.setOnClickListener {
            mListener!!.onStopDetection()
        }

        v.blank_button.setOnClickListener {
            startActivity(Intent(context, BlankActivity::class.java))
        }

        v.show_statistic.setOnClickListener{
            if(detection_stats.visibility == View.GONE){
                detection_stats.visibility = View.VISIBLE
                show_statistic.visibility = View.GONE
            }
            else{
                detection_stats.visibility = View.GONE
            }
        }
        return v
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
        batteryState = batteryEvent
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
