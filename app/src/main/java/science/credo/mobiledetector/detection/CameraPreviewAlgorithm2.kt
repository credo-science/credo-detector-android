package science.credo.mobiledetector.detection

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.util.Base64
import android.util.Log
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.credo.mobiledetector.CredoApplication
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.database.DataManager
import science.credo.mobiledetector.database.DetectionStateWrapper
import science.credo.mobiledetector.info.ConfigurationInfo
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.info.LocationInfo
import science.credo.mobiledetector.network.ServerInterface
import science.credo.mobiledetector.network.messages.DetectionRequest
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.min


class CameraPreviewAlgorithm2(private val mContext: Context) : CameraPreview() {
    private val mServerInterface = ServerInterface.getDefault(mContext)
    private val mLocationInfo: LocationInfo = LocationInfo.getInstance(mContext)
    private val globalDeviceState = (mContext.applicationContext as CredoApplication).globalDeviceState
    private val calibration = globalDeviceState.calibration
    private val mConfigurationWrapper = ConfigurationWrapper(mContext)

    companion object {
        val TAG = "CameraPreviewClbkNative"
        val aDataSize = 24
        var detectionStatsManager: DetectionStatsManager? = null
    }

    override fun onPreviewFrame(data: ByteArray, hCamera: Camera) {

        val timestamp = System.currentTimeMillis()

        val config = ConfigurationInfo(mContext)
        val sensorsState = (mContext.applicationContext as CredoApplication).detectorState
        val width: Int
        val height: Int

        try {
            val parameters = hCamera.parameters
            width = parameters.previewSize.width
            height = parameters.previewSize.height
        } catch (e: Exception) {
            Log.w(TAG, e)
            return
        }

        // Check calibration stage and turn on if need
        if (!calibration.running) {
            // Yes, calibration is need to run because latest calibration is too old
            if (timestamp - calibration.last > mConfigurationWrapper.calibrationFrequency) {
                Log.d(TAG, "Calibration: start")

                calibration.frames = 0
                calibration.stages = 0
                calibration.start = timestamp
                calibration.running = true
                calibration.pixelAvg = FloatArray(width * height)
                /*calibration.pixelMin = IntArray(width * height)
                calibration.pixelMax = IntArray(width * height)
                calibration.pixelFinalAvg = FloatArray(width * height)
                calibration.pixelFinalAvgMin = FloatArray(width * height)
                calibration.pixelFinalAvgMax = FloatArray(width * height)
                calibration.pixelFinalMin = IntArray(width * height)
                calibration.pixelFinalMax = IntArray(width * height)*/
            }
        }

        if (detectionStatsManager == null) {
            detectionStatsManager = DetectionStatsManager()
        }


        val analysisData = LongArray(aDataSize)

        if (calibration.running) {
            //synchronized(calibration) {
                // counting calibration stats
                val startTimer = System.currentTimeMillis()

                val ret = analysing(width, height, data)
                hCamera.addCallbackBuffer(data)

                Log.d(TAG, "Calibration: frame analysing time: ${System.currentTimeMillis() - startTimer}ms")

                if (ret >= mConfigurationWrapper.calibrationFrames) { // mConfigurationWrapper.calibrationFrames
                    Log.d(TAG, "Calibration: go to next stage: ${calibration.stages + 1}...")
                    exportToPng(width, height, 0, "calibration_${calibration.start}_stage_${calibration.stages}_avg")
                    exportToPng(width, height, 1, "calibration_${calibration.start}_stage_${calibration.stages}_min")
                    exportToPng(width, height, 2, "calibration_${calibration.start}_stage_${calibration.stages}_max")
                    exportToPng(width, height, 8, "calibration_${calibration.start}_stage_${calibration.stages}_diff")
                    /*val pavg = IntArray(width * height)
                    val pmin = IntArray(width * height)
                    val pmax = IntArray(width * height)
                    val pdiff = IntArray(width * height)
                    export(pavg, 0)
                    export(pmin, 1)
                    export(pmax, 2)
                    export(pdiff, 8)
                    doAsync {
                        writeIntArrayAsPng(width, height, pavg, "calibration_${calibration.start}_stage_${calibration.stages}_avg")
                        writeIntArrayAsPng(width, height, pmin, "calibration_${calibration.start}_stage_${calibration.stages}_min")
                        writeIntArrayAsPng(width, height, pmax, "calibration_${calibration.start}_stage_${calibration.stages}_max")
                        writeIntArrayAsPng(width, height, pdiff, "calibration_${calibration.start}_stage_${calibration.stages}_diff")
                    }*/
                    finishStage()
                    calibration.stages++
                }

                if (calibration.stages >= mConfigurationWrapper.calibrationStages) {
                    calibration.running = false
                    calibration.last = timestamp

                    exportToPng(width, height, 3, "calibration_${calibration.start}_avg")
                    exportToPng(width, height, 4, "calibration_${calibration.start}_avgmin")
                    exportToPng(width, height, 5, "calibration_${calibration.start}_avgmax")
                    exportToPng(width, height, 6, "calibration_${calibration.start}_min")
                    exportToPng(width, height, 7, "calibration_${calibration.start}_max")
                    exportToPng(width, height, 9, "calibration_${calibration.start}_avgdiff")
                    exportToPng(width, height, 10, "calibration_${calibration.start}_diff")

                    Log.d(TAG, "Calibration: finish")
                }

                /*synchronized(calibration) {
                for (i in 0..(width * height - 1)) {
                    val pixel = data[i].toPositiveInt()
                    if (calibration.frames == 0) {
                        calibration.pixelAvg!![i] = pixel.toFloat()
                        //calibration.pixelMin!![i] = pixel
                        //calibration.pixelMax!![i] = pixel
                    } else {
                        calibration.pixelAvg!![i] = (calibration.pixelAvg!![i] * calibration.frames + pixel.toFloat()) / (calibration.frames + 1)
                        /*if (calibration.pixelMin!![i] > pixel) {
                            calibration.pixelMin!![i] = pixel
                        }
                        if (calibration.pixelMax!![i] < pixel) {
                            calibration.pixelMax!![i] = pixel
                        }*/
                    }
                }
                calibration.frames++

                if (false) {//(calibration.frames >= mConfigurationWrapper.calibrationFrames) {
                    for (i in 0..(width * height - 1)) {
                        if (calibration.stages == 0) {
                            calibration.pixelFinalAvg!![i] = calibration.pixelAvg!![i]
                            calibration.pixelFinalAvgMin!![i] = calibration.pixelAvg!![i]
                            calibration.pixelFinalAvgMax!![i] = calibration.pixelAvg!![i]
                            calibration.pixelFinalMin!![i] = calibration.pixelMin!![i]
                            calibration.pixelFinalMax!![i] = calibration.pixelMax!![i]
                        } else {
                            calibration.pixelFinalAvg!![i] = (calibration.pixelFinalAvg!![i] * calibration.stages + calibration.pixelAvg!![i]) / (calibration.stages + 1)
                            calibration.pixelFinalMin!![i] = (calibration.pixelFinalMin!![i] * calibration.stages + calibration.pixelMin!![i]) / (calibration.stages + 1)
                            calibration.pixelFinalMax!![i] = (calibration.pixelFinalMax!![i] * calibration.stages + calibration.pixelMax!![i]) / (calibration.stages + 1)

                            if (calibration.pixelFinalAvgMin!![i] > calibration.pixelAvg!![i]) {
                                calibration.pixelFinalAvgMin!![i] = calibration.pixelAvg!![i]
                            }
                            if (calibration.pixelFinalAvgMax!![i] < calibration.pixelAvg!![i]) {
                                calibration.pixelFinalAvgMax!![i] = calibration.pixelAvg!![i]
                            }
                        }
                    }
                    calibration.frames = 0

                    toWriteAvg = calibration.pixelAvg
                    toWriteMin = calibration.pixelMin
                    toWriteMax = calibration.pixelMax

                    calibration.pixelAvg = FloatArray(width * height)
                    calibration.pixelMin = IntArray(width * height)
                    calibration.pixelMax = IntArray(width * height)
                    calibration.stages++
                }

                if (calibration.stages >= mConfigurationWrapper.calibrationStages) {
                    calibration.running = false
                    calibration.last = timestamp
                }

            }*/
            //}
        } else {

            doAsync {

                var loop = -1
                detectionStatsManager!!.frameAchieved(width, height)
                val hits = LinkedList<Hit>()

                while (loop < MAX_HITS_ONE_FRAME) {
                    loop++
                    val (max, maxIndex, sum, zeroes) = calcHistogram(data, width, height, config.blackFactor)

                    val average: Double = sum.toDouble() / (width * height).toDouble()
                    val blacks: Double = zeroes * 1000 / (width * height).toDouble()

                    if (loop == 0) {
                        detectionStatsManager!!.updateStats(max, average, blacks)
                    }

                    // frames not rejected conditions
                    val averageBrightCondition = average < config.averageFactor
                    val blackPixelsCondition = blacks >= config.blackCount

                    // found Hit condition
                    val brightestPixelCondition = max > config.maxFactor

                    if (averageBrightCondition && blackPixelsCondition) {
                        if (loop == 0) {
                            detectionStatsManager!!.framePerformed(max, average, blacks)
                        }

                        if (brightestPixelCondition) {

                            val centerX = maxIndex.rem(width).toInt()
                            val centerY = (maxIndex / width).toInt()

                            val margin = config.cropSize / 2
                            val offsetX = max(0, centerX - margin)
                            val offsetY = max(0, centerY - margin)
                            val endX = min(width, centerX + margin)
                            val endY = min(height, centerY + margin)

                            val cropBitmap = ImageConversion.yuv2rgb(data, width, height, offsetX, offsetY, endX, endY)
                            detectionStatsManager!!.hitRegistered()
                            val cropDataPNG = bitmap2png(cropBitmap)
                            val dataString = Base64.encodeToString(cropDataPNG, Base64.DEFAULT)
                            val location = mLocationInfo.getLocationData()

                            val hit = Hit(
                                    dataString,
                                    timestamp,
                                    location.latitude,
                                    location.longitude,
                                    location.altitude,
                                    location.accuracy,
                                    location.provider,
                                    width,
                                    height,
                                    centerX,
                                    centerY,
                                    max.toInt(),
                                    average,
                                    blacks,
                                    config.blackFactor,
                                    sensorsState.accX,
                                    sensorsState.accY,
                                    sensorsState.accZ,
                                    sensorsState.orientation,
                                    sensorsState.temperature
                            )
                            hits.add(hit)

                            fillHited(data, width, height, maxIndex.toInt(), config.cropSize)
                        } else {
                            break
                        }
                    } else {
                        if (loop == 0) {
                            detectionStatsManager!!.activeDetect(false)
                        }
                        break
                    }
                }

                uiThread {
                    hCamera.addCallbackBuffer(data)
                }
                detectionStatsManager!!.flush(mContext, false)

                if (hits.size > 0) {
                    val mDataManager: DataManager = DataManager.getDefault(mContext)

                    for (hit in hits) {
                        try {
                            mDataManager.storeHit(hit)
                        } catch (e: Exception) {
                            Log.e(TAG, "Can't store hit", e)
                        }
                    }
                    // FIXME: in separated service because "send and check as toSend=false" should be atomic
                    try {
                        mDataManager.sendHitsToNetwork(mServerInterface)
                    } catch (e: Exception) {
                        Log.w(TAG, "Can't sent hit to server", e)
                    }
                    mDataManager.closeDb()
                }
            }
        }
        doAutoCallibrationIfNeed()
    }


    external fun analysing(width: Int, height: Int, NV21FrameData: ByteArray) : Int
    external fun export(pixels: IntArray, file: Int)
    external fun clear()
    external fun finishStage()

    private fun doAutoCallibrationIfNeed() {
        val cw = ConfigurationWrapper(mContext)
        val config = ConfigurationInfo(mContext)
        if (!cw.autoCalibrationPerformed) {
            return
        }

        val ds = DetectionStateWrapper.getTotal(mContext)
        if (ds.performedFrames > 500) {
            Log.i("AutoCalibration", "average: ${ds.averageStats.average}")
            Log.i("AutoCalibration", "max: ${ds.maxStats.average}")
            config.averageFactor = minmax((ds.averageStats.average + 20).toInt(), 10, 60)
            config.blackFactor = minmax((ds.averageStats.average + 20).toInt(), 10, 60)
            config.maxFactor = minmax(max((ds.maxStats.average * 3).toInt(), 80), config.averageFactor, 160)
            cw.autoCalibrationPerformed = false
        }
    }

    fun minmax(v: Int, m: Int, a: Int): Int {
        return max(min(v, a), m)
    }

    override fun flush() {
        detectionStatsManager?.flush(mContext, true)
    }

    fun Byte.toPositiveInt() = toInt() and 0xFF

    data class CalcHistogramResult(val max: Int, val maxIndex: Int, val sum: Long, val zeroes: Int)
    fun calcHistogram(
        data: ByteArray,
        width: Int,
        height: Int,
        black: Int
    ) : CalcHistogramResult {
        var sum: Long = 0
        var max: Int = 0
        var maxIndex: Int = 0
        var zeros: Int = 0
        //var histogram = ByteArray(256)

        for (i in 0..(width*height - 1)) {
            val byte = data[i].toPositiveInt()
            //histogram[byte]++
            if (byte > 0) {
                sum += byte
                if (byte > max) {
                    max = byte
                    maxIndex = i
                }
            }
            if (byte <= black) {
                zeros++
            }
        }

        return CalcHistogramResult(max, maxIndex, sum, zeros)
    }

    fun bitmap2png (bitmap: Bitmap) : ByteArray {
        val pngData = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngData)
        return pngData.toByteArray()
    }

    fun writeBitmapAsPng(fileName: String, bitmap: Bitmap) {
        val pictures = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)}/CREDO"
        File(pictures).mkdirs()
        val file = File(pictures, "/$fileName.png")
        val os = file.outputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        os.close()
    }

    fun writeIntArrayAsPng(width: Int, height: Int, pixels: IntArray, fileName: String) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        writeBitmapAsPng(fileName, bitmap)
    }

    private fun exportToPng(width: Int, height: Int, number: Int, fileName: String) {
        val pdiff = IntArray(width * height)
        export(pdiff, number)
        writeIntArrayAsPng(width, height, pdiff, fileName)
    }

    // FIXME: limit to ~5
    fun fillHited(data: ByteArray, width: Int, height: Int, maxPosition: Int, sideLength: Int){

        //Point (maxX,maxY) is center(brightest pixel) of hit
        val maxX = maxPosition.rem(width)
        val maxY = maxPosition / width

        //Point (x,y) is upper-left corner of square with we want to fill
        var x = maxX - sideLength / 2
        var y = maxY - sideLength / 2


        when {
            x < 0 -> x = 0
            x >= width - sideLength -> x = width - sideLength
        }

        when {
        //We want to make sure that upper-left point of square is at least sideLength from bottom and right side of image
            y < 0 -> y = 0
            y >= height - sideLength -> y = height - sideLength
        }

        //Loops iterates from upper-left point sideLength times
        for (i in y..y + sideLength) {
            for (j in x..x + sideLength) {
                data[i * width + j] = 0
            }
        }

    }

}
