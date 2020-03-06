package science.credo.mobiledetector.detector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_detector.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.detector.camera2.Camera2DetectorFragment
import science.credo.mobiledetector.detector.old.OldDetectorFragment
import science.credo.mobiledetector.settings.*
import science.credo.mobiledetector.utils.Prefs

class DetectorActivity : AppCompatActivity() {

    companion object {
        const val RC_SETTINGS = 327
        fun intent(context: Context): Intent {
            val intent = Intent(context, DetectorActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detector)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_toolbar_back)

        if (checkSettings() != null) {
            startDetector()
        } else {
            startActivityForResult(SettingsActivity.intent(this, true), RC_SETTINGS)
        }


    }

    private fun checkSettings(): BaseSettings? {
        var settings: BaseSettings? = Prefs.get(this, OldCameraSettings::class.java)
        if (settings != null) {
            return settings
        }
        settings = Prefs.get(this, Camera2ApiSettings::class.java)
        if (settings != null) {
            return settings
        }

        return null


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SETTINGS) {
            if (checkSettings() != null) {
                startDetector()
            } else {
                finish()
            }
        }
    }

    private fun startDetector() {

        val api = Prefs.get(this, CameraApi::class.java)
        val fragment: Fragment? = when (api!!) {
            CameraApi.OLD -> OldDetectorFragment.instance()
            CameraApi.CAMERA2 -> Camera2DetectorFragment.instance()
            CameraApi.NDK -> null
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.containerFragments, fragment!!)
        transaction.addToBackStack(fragment::class.java.simpleName)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    var isClosing = false
    override fun onBackPressed() {
        if (!isClosing) {
            isClosing = true
            GlobalScope.launch(Dispatchers.Main) {
                viewProgress.visibility = View.VISIBLE
                (supportFragmentManager.fragments[0] as BaseDetectorFragment).stopCamera()
                delay(4000)
                viewProgress.visibility = View.GONE
                finish()
            }
        }
    }

}