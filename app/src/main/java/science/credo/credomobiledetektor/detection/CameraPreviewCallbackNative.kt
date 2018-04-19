package science.credo.credomobiledetektor.detection

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.support.v8.renderscript.RenderScript
import android.util.Base64
import android.util.Log
import io.github.silvaren.easyrs.tools.Nv21Image
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.doAsync
import science.credo.credomobiledetektor.database.DataManager
import science.credo.credomobiledetektor.events.FrameEvent
import science.credo.credomobiledetektor.info.ConfigurationInfo
import science.credo.credomobiledetektor.info.LocationInfo
import java.io.ByteArrayOutputStream
import kotlin.math.round

class CameraPreviewCallbackNative(private val mContext: Context) : Camera.PreviewCallback {
    private var state = FrameEvent()
    private var lastFlushStats = 0L
    private var flushCount = 0
    private var stateMax = 0L
    private var statAverage = 0.0
    private var zerosAverage = 0.0

    private val mDataManager: DataManager = DataManager.getInstance(mContext)
    private val mLocationInfo: LocationInfo = LocationInfo.getInstance(mContext)

    companion object {
        val TAG = "CameraPreviewClbkNative"
        val aDataSize = 24
        //val cropSize = 20
    }

    val rs = RenderScript.create(mContext)

    override fun onPreviewFrame(data: ByteArray, hCamera: Camera) {

        val config = ConfigurationInfo(mContext)

        val timestamp = System.currentTimeMillis()
        if (state.startDetection == 0L) {
            state.startDetection = timestamp
        }

        state.lastUpdate = timestamp
        state.frames++
        flushCount++

        val updateStats = lastFlushStats / 1000L != timestamp / 1000L

        val parameters = hCamera.parameters
        val width = parameters.previewSize.width
        val height = parameters.previewSize.height
        val analysisData = LongArray(aDataSize)

        var multiHit = false
        var multiHitEnd = true

        do {

            calcHistogram(data, analysisData, width, height, config.blackFactor)

            val max = analysisData[aDataSize - 1]
            val maxIndex = analysisData[aDataSize - 2]
            val sum = analysisData[aDataSize - 3]
            val zeroes = analysisData[aDataSize - 4]
            val average: Double = sum.toDouble() / (width * height).toDouble()

            statAverage += average
            zerosAverage += zeroes
            stateMax = kotlin.math.max(stateMax, max)

//        Log.d(TAG, analysisData.joinToString())

            if (max > config.maxFactor && average < config.averageFactor && (zeroes * 1000 / (width * height)) >= config.blackCount) {
                val bitmap = yuv2bitmap(data, width, height)
                fillHited(data,width, height, maxIndex.toInt(), config.cropSize)
                doAsync {
                    //                Log.d(TAG, analysisData.joinToString())
//                Log.d(TAG, "max: $max, maxIndex: $maxIndex (${maxIndex.toInt()}), sum: $sum, zeroes: $zeroes, averge: $average")

                    state.lastHit = timestamp
                    val cropBitmap = cropBitmap(bitmap, width, height, maxIndex.toInt(), config.cropSize)
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
                            height

                    )
                    mDataManager.storeHit(hit)

                    Log.d(TAG, "Image detected and stored in DB")

                }
                multiHit=true
            } else {
                multiHitEnd = false
            }

        }while(multiHitEnd)

        if(multiHit){
            val size = width * height + width * height / 2
            val callbackBuffer = ByteArray(size)
            hCamera.addCallbackBuffer(callbackBuffer)
        }else{
            hCamera.addCallbackBuffer(data)
        }

        if (updateStats) {
            state.height = height
            state.width = width
            state.max = stateMax
            state.average = statAverage / flushCount
            state.zeros = zerosAverage * 1000 / (width * height * flushCount)
            EventBus.getDefault().post(state.copy())
            flushCount = 0
            statAverage = 0.0
            stateMax = 0
            zerosAverage = 0.0
            lastFlushStats = timestamp
        }
    }

    external fun calcHistogram (data: ByteArray, analysisData: LongArray, width: Int, height: Int, black: Int)

    fun yuv2bitmap (data: ByteArray, width: Int, height: Int) : Bitmap {
        return Nv21Image.nv21ToBitmap(rs, data, width,height)
    }

    fun bitmap2png (bitmap: Bitmap) : ByteArray {
        val pngData = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngData)
        return pngData.toByteArray()
    }

    fun yuv2png (data: ByteArray, width: Int, height: Int) : ByteArray {
        val bitmap = Nv21Image.nv21ToBitmap(rs, data, width,height)
        val pngData = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngData)
        return pngData.toByteArray()
    }

    fun cropBitmap(bitmap: Bitmap, width: Int, height: Int, maxPosition: Int, sideLength: Int): Bitmap {
        // position of max bright pixel
        val maxX = maxPosition.rem(width)
        val maxY = maxPosition/width

        var x = maxX - sideLength/2
        var y = maxY - sideLength/2

        when {
            x < 0 -> x = 0
            y < 0 -> y = 0
            x >= width - sideLength -> x = width - sideLength
            y >= height - sideLength -> y = height - sideLength
        }

        return Bitmap.createBitmap(bitmap, x, y, sideLength, sideLength)
    }

    fun fillHited(data: ByteArray, width: Int, height: Int, maxPosition: Int, sideLength: Int){

        val maxX = maxPosition.rem(width)
        val maxY = maxPosition/width

        var x = maxX - sideLength/2
        var y = maxY - sideLength/2

        when {
            x < 0 -> x = 0
            y < 0 -> y = 0
            x >= width - sideLength -> x = width - sideLength
            y >= height - sideLength -> y = height - sideLength
        }

        for(i in y..y+sideLength){
            for(j in x..x+sideLength){
                data[i*width+j]=0
            }
        }

    }

}
