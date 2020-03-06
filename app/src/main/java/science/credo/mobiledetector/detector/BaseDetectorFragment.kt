package science.credo.mobiledetector.detector

import androidx.fragment.app.Fragment

abstract class BaseDetectorFragment : Fragment() {

    var cameraInterface: CameraInterface? = null

    fun stopCamera() {
        cameraInterface?.stop()
    }

}