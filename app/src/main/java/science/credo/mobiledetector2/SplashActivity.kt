package science.credo.mobiledetector2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import science.credo.mobiledetector2.login.LoginActivity
import science.credo.mobiledetector2.utils.UiUtils

class SplashActivity : AppCompatActivity() {

    companion object {
        const val RC_PERMISSIONS = 1001
        fun intent(context: Context): Intent {
            return Intent(context, SplashActivity::class.java)
        }


    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        UiUtils.initScreenDimensions(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ivLogo.animate()
                .setDuration(2500)
                .alpha(1f)
                .withEndAction {
                    checkPermissions()
                }
                .start()
        } else {
            ivLogo.alpha = 1f
            checkPermissions()
        }


    }

    var askedForBattery = false
    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            GlobalScope.launch {
                delay(500)
                if (!Settings.canDrawOverlays(this@SplashActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, 0)
                    return@launch
                }
            }
        }
        if (askedForBattery) {
            afterPermissions(true)
        }
    }

    fun askForBatteryOptimization(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            println("===========power  ${pm.isIgnoringBatteryOptimizations(packageName)}")
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val i = Intent()
                i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                i.setData(Uri.parse("package:$packageName"))
                startActivity(i)
                false
            } else {
                true
            }
        } else {
            true
        }
    }


    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED

            ) {
                askForPermission()
            } else {
                afterPermissions(true)
            }
        } else {
            afterPermissions(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun askForPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            ), RC_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permissionsGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = false
                break
            }
        }
        afterPermissions(permissionsGranted)
    }

    private fun afterPermissions(granted: Boolean) {
        if (!granted) {
            finish()
        } else {
            if (askForBatteryOptimization()) {

                startActivity(LoginActivity.intent(this))
                finish()

            } else {
                askedForBattery = true
            }

        }
    }
}