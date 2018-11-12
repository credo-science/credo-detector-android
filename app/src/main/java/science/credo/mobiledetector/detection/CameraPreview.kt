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
import kotlin.math.max
import kotlin.math.min


abstract class CameraPreview : Camera.PreviewCallback {
    abstract fun flush()
}
