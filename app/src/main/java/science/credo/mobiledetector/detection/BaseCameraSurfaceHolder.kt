package science.credo.mobiledetector.detection

import android.view.SurfaceHolder

interface BaseCameraSurfaceHolder : SurfaceHolder.Callback {
    fun flush()
}
