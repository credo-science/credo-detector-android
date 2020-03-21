package science.credo.mobiledetector.detector.camera2

import android.content.Context
import android.graphics.ImageFormat
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import science.credo.mobiledetector.detector.CameraInterface
import science.credo.mobiledetector.detector.Frame
import science.credo.mobiledetector.detector.State
import science.credo.mobiledetector.detector.camera2.RawFormatCalibrationResult.Companion.AMPLIFIER
import science.credo.mobiledetector.detector.old.JniWrapper
import science.credo.mobiledetector.settings.Camera2ApiSettings
import science.credo.mobiledetector.utils.Statistics
import java.lang.Exception


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RawFormatCalibrationFinder(
    val context: Context,
    val configuration: Camera2ApiSettings,
    val callback: CalibrationCallback
) : CameraInterface.FrameCallback {

    val LENGHT = 60000/configuration.exposureInMillis

    companion object {
        const val STATUS_IN_PROGRESS = 1
        const val STATUS_NONE = 2
        const val STATUS_COVER_ERROR = 3

    }

    public interface CalibrationCallback {
        fun onStatusChanged(state: State, msg: String, progress: Int, avgNoise: Int)
        fun onCalibrationSuccess(
            calibrationResult: RawFormatCalibrationResult
        )

        fun onCalibrationFailed()
    }

    var clusterFactorWidth = 2
    var clusterFactorHeight = 1
    var counter = 0
    var max: IntArray = IntArray(LENGHT)
    var avgs: IntArray = IntArray(LENGHT)
    private lateinit var cameraUtil: CameraInterface

    var calibration: RawFormatCalibrationResult? = null

    public fun start() {

        println(">>>>>>>> start lenght: $LENGHT")

        if (configuration.imageFormat == ImageFormat.RAW_SENSOR) {
            clusterFactorWidth = 2
            clusterFactorHeight = 2
        }

        tryNext()

    }


    private fun tryNext() {
        configuration.scaledHeight = configuration.height / clusterFactorHeight
        configuration.scaledWidth = configuration.width / clusterFactorWidth


        ignoreFirstsFrame = 0
        GlobalScope.launch {
            delay(2000)
            cameraUtil =
                Camera2PostConfigurationInterface(configuration, this@RawFormatCalibrationFinder)
            cameraUtil.start(context)
        }


    }

    var ignoreFirstsFrame = 0

    override fun onFrameReceived(frame: Frame) {

        GlobalScope.launch {
            if (ignoreFirstsFrame < 3) {
                ignoreFirstsFrame++
                return@launch
            }

            if (JniWrapper.isBusy) {
                return@launch
            }
            val frameResult = JniWrapper.calculateFrame(
                frame.byteArray,
                frame.width,
                frame.height,
                clusterFactorWidth,
                clusterFactorHeight,
                2
            )

            if (frameResult.avg <= 50) {
                callback.onStatusChanged(
                    State.CALIBRATION,
                    "${clusterFactorWidth}x$clusterFactorHeight",
                    ((counter.toFloat()/LENGHT)*100).toInt(),
                    frameResult.avg
                )
                if (counter >= LENGHT) {
                    return@launch
                }
                max[counter] = frameResult.max
                avgs[counter] = frameResult.avg
                counter++
                if (counter == LENGHT) {
                    println(">>>>>>>> c progress: $LENGHT")
                    counter = 0
                    cameraUtil.stop()
                    val stat = Statistics(max)
                    println("======${clusterFactorWidth}x$clusterFactorHeight stdDev : " + stat.stdDev)
                    if (configuration.imageFormat == ImageFormat.RAW_SENSOR) {
                        if (stat.stdDev < 2) {
                            callback.onCalibrationSuccess(
                                RawFormatCalibrationResult(
                                    clusterFactorWidth,
                                    clusterFactorHeight,
                                    (stat.mean * AMPLIFIER).toInt(),
                                    getMax(avgs)
                                )


                            )
                        } else {
                            changeClusterFactors()
                            if (clusterFactorWidth * clusterFactorHeight > 64) {
                                callback.onCalibrationFailed()
                            } else {
                                tryNext()
                            }
                        }
                    } else {
                        if (stat.stdDev < 2) {
                            callback.onCalibrationSuccess(
                                RawFormatCalibrationResult(
                                    clusterFactorWidth,
                                    clusterFactorHeight,
                                    (stat.mean + 15).toInt(),
                                    getMax(avgs)
                                )
                            )
                        } else {
                            changeClusterFactors()
                            if (clusterFactorWidth * clusterFactorHeight > 64) {
                                println("====== stop ")
                            } else {
                                tryNext()
                            }
                        }

                    }

                }
            } else {
                callback.onStatusChanged(State.NOT_COVERED, "", 0, frameResult.avg)
            }

        }

    }

    fun changeClusterFactors() {

        if (clusterFactorWidth <= clusterFactorHeight) {
            clusterFactorWidth = findNextDivisor(configuration.width, clusterFactorWidth)
        } else {
            clusterFactorHeight = findNextDivisor(configuration.height, clusterFactorHeight)
        }

    }

    fun findNextDivisor(number: Int, currentDivisor: Int): Int {

        for (i in currentDivisor + 1 until number) {
            if (number % i == 0) {
                return i
            }
        }
        return 0

    }

    fun getMax(array: IntArray): Int {
        var max = array[0]
        for (i in 1 until array.size) {
            if (array[i] > max) {
                max = array[i]
            }
        }
        return max
    }

    fun stop() {
        try {
            cameraUtil.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}