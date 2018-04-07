package science.credo.credomobiledetektor.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_detection.*
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.android.synthetic.main.fragment_status.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import science.credo.credomobiledetektor.CredoApplication

import science.credo.credomobiledetektor.R
import science.credo.credomobiledetektor.database.DataManager
import science.credo.credomobiledetektor.events.BatteryEvent
import science.credo.credomobiledetektor.events.DetectorStateEvent
import science.credo.credomobiledetektor.events.FrameEvent
import science.credo.credomobiledetektor.info.*
import java.text.SimpleDateFormat
import java.util.*


class StatusFragment : Fragment() {

    private val mReceiver = PowerConnectionReceiver()
    private var detectorState: DetectorStateEvent = DetectorStateEvent()
    private var frameEvent = FrameEvent()
    private var batteryState = BatteryEvent()

    private var mListener: OnFragmentInteractionListener? = null

    private fun detectionText() : String {
        return when (detectorState.running) {
            false -> context!!.getString(R.string.status_fragment_switched_off)
            true -> when (detectorState.cameraOn) {
                true -> context!!.getString(R.string.status_fragment_running)
                false -> context!!.getString(R.string.status_fragment_hold)
            }
        }
    }

    private fun fillInValuesOnPage() {
        val ui = UserInfo.getNewInstance(context!!)

        name_text.text = ui.name
        email_text.text = ui.email
        team_text.text = ui.team

        detection_text.text = detectionText()
        detections_label.text = getString(R.string.status_fragment_detections, DataManager.TRIMPERIOD_HITS_DAYS)

        val dm = DataManager.getInstance(context!!)
        detections_text.text = (dm.getCachedHitsNumber() + dm.getHitsNumber()).toString()

        start_text.text = if (frameEvent.startDetection > 0)
            dateFormat.format(frameEvent.startDetection)
        else
            ""

        latest_text.text = if (frameEvent.lastUpdate > 0)
            dateFormat.format(frameEvent.lastUpdate)
        else
            ""

        hit_text.text = if (frameEvent.lastHit > 0)
            dateFormat.format(frameEvent.lastHit)
        else
            ""

        frame_size_text.text = "${frameEvent.width}x${frameEvent.height}"
        frame_count_text.text = frameEvent.frames.toString()

        bright_text.text = "%.2f".format(frameEvent.average)
        max_text.text = frameEvent.max.toString()

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
        temperature_text.text = "%d °C".format(detectorState.temperature)
        acc_text.text = "X:%d Y:%d Z:%d".format(detectorState.accX, detectorState.accY, detectorState.accZ)

        black_text.text = "%.0f‰".format(frameEvent.zeros)
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
        v.toggle_detection.setOnClickListener {
            when (detectorState.running) {
                true -> mListener?.onStopDetection()
                false -> mListener?.onStartDetection()
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
    fun onFrameEvent(frameEvent: FrameEvent) {
        this.frameEvent = frameEvent
        fillInValuesOnPage()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBatteryEvent(batteryEvent: BatteryEvent) {
        batteryState = batteryEvent
        fillInValuesOnPage()
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
    }
}
