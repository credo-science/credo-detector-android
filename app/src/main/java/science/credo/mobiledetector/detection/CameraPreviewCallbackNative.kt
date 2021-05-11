package science.credo.mobiledetector.detection

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
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
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min

const val MAX_HITS_ONE_FRAME = 5


class CameraPreviewCallbackNative(private val mContext: Context) : Camera.PreviewCallback {
    private val mServerInterface = ServerInterface.getDefault(mContext)
    private val mLocationInfo: LocationInfo = LocationInfo.getInstance(mContext)


    companion object {
        val TAG = "CameraPreviewClbkNative"
        val aDataSize = 24
        var detectionStatsManager: DetectionStatsManager? = null

        private var benchmarkSumMs: AtomicInteger = AtomicInteger(0)
        private var benchmarkCount: AtomicInteger = AtomicInteger(0)
        var benchmark: Int = 0

        private var lastFpsSecond: Long = 0
        private var lastFps: Int = 0
        var fps: Int = 0
    }

    override fun onPreviewFrame(data: ByteArray, hCamera: Camera) {

        val timestamp = System.currentTimeMillis()
        if (timestamp / 1000 != lastFpsSecond) {
            lastFpsSecond = timestamp / 1000
            fps = lastFps
            lastFps = 0
        } else {
            lastFps++
        }

        if (detectionStatsManager == null) {
            detectionStatsManager = DetectionStatsManager()
        }

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

        val analysisData = LongArray(aDataSize)

        doAsync {
            val benchmarkStart = System.currentTimeMillis()

            var loop = -1
            detectionStatsManager!!.frameAchieved(width, height)
            val hits = LinkedList<Hit>()
            var original: ByteArray? = null

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

                        val centerX = maxIndex.rem(width)
                        val centerY = (maxIndex / width)

                        val margin = config.cropSize / 2
                        var offsetX = max(0, centerX - margin)
                        var offsetY = max(0, centerY - margin)
                        var endX = min(width, centerX + margin)
                        var endY = min(height, centerY + margin)

                        // form hits on edges move the crop window from over edge to edge
                        if (offsetX == 0) {
                            endX = config.cropSize
                        }

                        if (endX == width) {
                            offsetX = width - config.cropSize
                        }

                        if (offsetY == 0) {
                            endY = config.cropSize
                        }

                        if (endY == height) {
                            offsetY = height - config.cropSize
                        }

                        // lazy copy original for mark extracted hits
                        if (original == null) {
                            original = data.clone()
                        }

                        val cropBitmap = ImageConversion.yuv2rgb(original, width, height, offsetX, offsetY, endX, endY)
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

                        fillHited(data, width, offsetX, offsetY, endX, endY)
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

            var bc = benchmarkCount.addAndGet(1)
            var bs = benchmarkSumMs.addAndGet((System.currentTimeMillis() - benchmarkStart).toInt())

            if (bc == 100) {
                benchmark = bs / 100;
                benchmarkCount = AtomicInteger(0)
                benchmarkSumMs = AtomicInteger(0)
            }
        }
        doAutoCallibrationIfNeed()
    }

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

    fun flush() {
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

        for (i in 0 until width*height) {
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

    fun fillHited(data: ByteArray, width: Int, offsetX: Int, offsetY: Int, endX: Int, endY: Int){
        //Loops iterates from upper-left point sideLength times
        for (i in offsetY until endY) {
            for (j in offsetX until endX) {
                data[i * width + j] = 0
            }
        }
    }
}
