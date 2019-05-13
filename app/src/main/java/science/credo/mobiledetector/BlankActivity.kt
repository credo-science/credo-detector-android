package science.credo.mobiledetector

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager

class BlankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_blank)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        onBackPressed()
        return super.onTouchEvent(event)
    }
}
