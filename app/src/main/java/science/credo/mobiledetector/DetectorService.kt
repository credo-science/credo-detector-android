package science.credo.mobiledetector

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.hardware.*
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.preference.PreferenceManager
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.detection.CameraSurfaceHolder
import science.credo.mobiledetector.events.BatteryEvent
import science.credo.mobiledetector.events.DetectorStateEvent
import science.credo.mobiledetector.info.ConfigurationInfo
import science.credo.mobiledetector.info.PowerConnectionReceiver


class DetectorService : Service(), SharedPreferences.OnSharedPreferenceChangeListener, SensorEventListener {
    companion object {
        val TAG = "DetectorService"
        var count = 0
    }

    private val state = DetectorStateEvent(false)
    private var batteryState = BatteryEvent()

    //private var mWakeLock: PowerManager.WakeLock? = null;
    private var mCamera: Camera? = null;
    private var mSurfaceView: SurfaceView? = null
    private var mWindowManager: WindowManager? = null
    private val mReceiver = PowerConnectionReceiver()
    private var mConfigurationInfo: ConfigurationInfo? = null
    private var mSensorManager: SensorManager? = null

    private var mCameraSurfaceHolder: CameraSurfaceHolder? = null

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        Log.d(TAG,"onCreate: " + ++count)
        super.onCreate()
        state.running = true
        EventBus.getDefault().register(this)
        emitStateChange()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        mConfigurationInfo = ConfigurationInfo(baseContext)
        //mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CREDO_LOCK")
        //mWakeLock?.acquire()

        startCamera()
        Log.d(TAG,"onStartCommand " + count)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        val intent: Intent = registerReceiver(mReceiver, filter)!!
        batteryState = PowerConnectionReceiver.parseIntent(intent)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //val temperatureSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        val accelerometerSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val orientationSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        //mSensorManager?.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL)

        PreferenceManager.getDefaultSharedPreferences(applicationContext).registerOnSharedPreferenceChangeListener(this)

        return START_STICKY;
    }

    override fun onDestroy() {
        mSensorManager?.unregisterListener(this)
        //mWakeLock?.release()
        stopCamera()
        Log.d(TAG,"onDestroy: " + --count)

        unregisterReceiver(mReceiver)
        PreferenceManager.getDefaultSharedPreferences(applicationContext).unregisterOnSharedPreferenceChangeListener(this)

        state.running = false
        emitStateChange()
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun checkConditionsAndEmitState() : Boolean {
        if (!mConfigurationInfo!!.isDetectionOn) {
            return false
        }

        if (mConfigurationInfo!!.isChargerOnly && !batteryState.isCharging) {
            setState(DetectorStateEvent.StateType.Warning, getString(R.string.warning_condition_charging))
            return false
        }

        if (batteryState.batteryPct < mConfigurationInfo!!.batteryLevel) {
            setState(DetectorStateEvent.StateType.Warning, getString(R.string.warning_condition_low_battery))
            return false
        }

        if (state.temperature > mConfigurationInfo!!.maxTemperature) {
            setState(DetectorStateEvent.StateType.Warning, getString(R.string.warning_condition_too_heat))
            return false
        }

        if (state.type < DetectorStateEvent.StateType.Error) {
            setState(DetectorStateEvent.StateType.Normal, "")
        }

        return true
    }

    fun startStopOnConditionChange() {
        val canProcess = checkConditionsAndEmitState()
        Log.d(TAG,"startStopOnConditionChange: canProcess: $canProcess")
        when {
            canProcess && !state.cameraOn -> startCamera()
            !canProcess && state.cameraOn -> stopCamera()
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        startStopOnConditionChange()
    }

    fun startCamera() {
        val cw = ConfigurationWrapper(this)

        Log.d(TAG, "startCamera: " + count)
        if (!checkConditionsAndEmitState()) {
            Log.d(TAG, "startCamera: start not allowed")
            return
        }
        if (state.cameraOn) {
            Log.d(TAG,"startCamera: camera already running")
            return
        }

        Log.d(TAG, "startCamera: starting camera")
        try {
            state.cameraOn = false
            mCamera = Camera.open(cw.cameraNumber)
            state.cameraOn = true
        } catch (e: RuntimeException) {
            if (CredoApplication.isEmulator()) {
                //Toast.makeText(this, R.string.error_emulator, Toast.LENGTH_LONG).show()
                setState(DetectorStateEvent.StateType.Error, getString(R.string.error_emulator))
            } else {
                val msg = getString(R.string.error_camera, e.message)
                //Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                setState(DetectorStateEvent.StateType.Error, msg)
            }
            return
        }
        val parameters: Camera.Parameters = mCamera!!.parameters;
        parameters.setRecordingHint(true)
        val sizes = parameters.supportedPreviewSizes
        //val index = sizes.size/2 // ~medium resolution
        val index = cw.frameSize // if (ConfigurationInfo(this).isFullFrame) 0 else sizes.size/2
        for (size in sizes) {
            Log.d(TAG, "width: ${size.width}, height: ${size.height}")
        }
        Log.d(TAG,"will use: ${sizes[index].width}, height: ${sizes[index].height}")
        parameters.setPreviewSize(sizes[index].width, sizes[index].height)
        //parameters.previewFormat =
        mCamera?.parameters = parameters;

        mSurfaceView = SurfaceView(this)
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            2,
            2,
            -5000,
            5000,
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE + WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

        mCameraSurfaceHolder = CameraSurfaceHolder(mCamera!!, baseContext)
        mSurfaceView?.holder?.addCallback(mCameraSurfaceHolder)

        mWindowManager?.addView(mSurfaceView, params)
        mSurfaceView?.setZOrderOnTop(false)
        mSurfaceView?.visibility = View.VISIBLE
        setState(DetectorStateEvent.StateType.Normal, getString(R.string.status_fragment_running))
    }

    fun stopCamera() {
        Log.d(TAG,"stopCamera")
        if (!state.cameraOn) {
            Log.d(TAG, "stopCamera: camera already stopped")
            return
        }

        state.cameraOn = false
        mSurfaceView?.holder?.removeCallback(mCameraSurfaceHolder)
        mWindowManager?.removeView(mSurfaceView)
        mCamera?.stopPreview()
        mCameraSurfaceHolder?.flush()
        mCamera?.release()
        emitStateChange()
    }

    private fun setState(type: DetectorStateEvent.StateType, msg: String) {
        state.status = msg
        state.type = type
        emitStateChange()
    }

    private fun emitStateChange() {
        EventBus.getDefault().post(state.copy())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            state.accX = event.values[0]
            state.accY = event.values[1]
            state.accZ = event.values[2]
        /*} else if (event?.sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            state.temperature = event.values[0].toInt()
            Log.d(TAG,"TYPE_AMBIENT_TEMPERATURE: ${state.temperature}")*/
        } else if (event?.sensor?.type == Sensor.TYPE_ORIENTATION) {
            state.orientation = event.values[0]
        }

        //startStopOnConditionChange()
        //emitStateChange()
    }

    @Subscribe
    fun onBatteryEvent(batteryEvent: BatteryEvent) {
        batteryState = batteryEvent
        if (batteryState.temperature != 0) {
            state.temperature = batteryState.temperature
        }
        startStopOnConditionChange()
    }
}
