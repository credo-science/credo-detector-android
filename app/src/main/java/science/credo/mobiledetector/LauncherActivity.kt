package science.credo.mobiledetector

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.Html
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_launcher.*
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.database.UserInfoWrapper
import android.content.DialogInterface
import android.os.Build
import android.provider.Settings
import android.support.v7.app.AlertDialog


const val REQUEST_MAIN = 1
const val REQUEST_SIGN = 2

class LauncherActivity : AppCompatActivity() {

    companion object {
        private val CAMERA_PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                //Manifest.permission.WAKE_LOCK
        )

        private val REQUEST_PERMISSION_PHONE_STATE = 1
        private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469

        /**
         * Tells whether all the necessary permissions are granted to this app.
         *
         * @return True if all the required permissions are granted.
         */
        fun hasAllPermissionsGranted(context: AppCompatActivity): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (permission in CAMERA_PERMISSIONS) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }

                val packageName = context.packageName

                // todo: found way to not need overlay in Android >=P
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))
                    context.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                }

            }

            return true
        }
    }

    var debugClicksCount = 0
    val debugClicksToActivate = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        val toast = Toast.makeText(this, "", Toast.LENGTH_LONG)

        login_button.setOnClickListener {
            startActivityForResult(Intent(this@LauncherActivity, LoginActivity::class.java), REQUEST_SIGN)
            activate_email_message.visibility = View.GONE
        }

        register_button.setOnClickListener {
            startActivityForResult(Intent(this@LauncherActivity, RegisterActivity::class.java), REQUEST_SIGN)
        }

        remember_password_button.setOnClickListener {
            val endpoint = ConfigurationWrapper(this@LauncherActivity).endpoint.replace("/api/v2", "")
            val href = "$endpoint/web/password_reset/"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(href))
            startActivity(browserIntent)
        }

        debug_mode_off_button.setOnClickListener {
            ConfigurationWrapper(this@LauncherActivity).endpoint = ConfigurationWrapper.defaultEndpoint
            debugClicksCount = 0
            endpoint_layout.visibility = View.GONE
            debug_mode_off_button.visibility = View.GONE
        }

        logo_image.setOnClickListener {
            debugClicksCount++
            if (debugClicksCount >= debugClicksToActivate) {
                endpoint_layout.visibility = View.VISIBLE
                debug_mode_off_button.visibility = View.VISIBLE
                if (debugClicksCount == debugClicksToActivate) {
                    toast.setText(R.string.launcher_toast_debug_activated)
                    toast.show()
                }
            } else if (debugClicksCount > 2) {
                toast.setText(
                        getString(
                                R.string.launcher_toast_debug_activating,
                                debugClicksToActivate - debugClicksCount
                        )
                )
                toast.show()
            }
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (hasAllPermissionsGranted(this)) {
            launchSpecificActivity()
            debugClicksCount = 0
            endpoint_input.setText(ConfigurationWrapper(this).endpoint, TextView.BufferType.EDITABLE)
        } else {
            requestPermissions()
        }
    }

    override fun onPause() {
        ConfigurationWrapper(this).endpoint = endpoint_input.text.toString()
        super.onPause()
    }

    private fun launchSpecificActivity() {
        val pref = UserInfoWrapper(this)

        if (!pref.token.isEmpty()) {
            startActivityForResult(Intent(this, MainActivity::class.java), REQUEST_MAIN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MAIN) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            }
        } else if (requestCode == REQUEST_SIGN && resultCode == Activity.RESULT_FIRST_USER) {
            activate_email_message.visibility = View.VISIBLE
        } else if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    /**
     * Requests permissions necessary to use camera and save pictures.
     */
    private fun requestPermissions() {
        if (shouldShowRationale()) {
            showExplanation()
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_PERMISSION_PHONE_STATE)
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting the permissions.
     *
     * @return True if the UI should be shown.
     */
    private fun shouldShowRationale(): Boolean {
        for (permission in CAMERA_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true
            }
        }
        return false
    }

    private fun showExplanation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
                .setMessage("Permision are needs")

        builder.setPositiveButton(android.R.string.ok) {_, _ -> requestPermissions()}
        builder.create().show()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_PHONE_STATE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
