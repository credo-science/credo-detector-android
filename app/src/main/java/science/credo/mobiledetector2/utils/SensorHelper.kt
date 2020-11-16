package science.credo.mobiledetector2.utils

import android.app.Service
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

object SensorHelper : SensorEventListener {

    var accX = 0f
    var accY = 0f
    var accZ = 0f
    var orientationX =0f
    var orientationY =0f
    var orientationZ =0f
    var temperature = 0
    var orientation = 0f

    fun init(context: Context) {
        val sm: SensorManager = context.getSystemService(Service.SENSOR_SERVICE) as SensorManager

        val gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val temperatureSensor = sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        val accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val orientationSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        sm.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        sm.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sm.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sm.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL)


    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when {
            event?.sensor?.type == Sensor.TYPE_ACCELEROMETER -> {
                accX = event.values[0]
                accY = event.values[1]
                accZ = event.values[2]
            }
            event?.sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE -> temperature =
                event.values[0].toInt()
            event?.sensor?.type == Sensor.TYPE_ORIENTATION -> orientation = event.values[0]
            event?.sensor?.type == Sensor.TYPE_GYROSCOPE -> {
                orientationX = event.values[0]
                orientationY = event.values[1]
                orientationZ = event.values[2]
            }
        }
    }
}