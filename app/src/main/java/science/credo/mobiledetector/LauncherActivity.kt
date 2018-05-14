package science.credo.mobiledetector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_launcher.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.database.UserInfoWrapper

const val REQUEST_MAIN = 1
const val REQUEST_SIGN = 2

class LauncherActivity : AppCompatActivity() {

    var debugClicksCount = 0
    val debugClicksToActivate = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        val toast = Toast.makeText(this, "", Toast.LENGTH_LONG)

        login_button.onClick {
            startActivityForResult(Intent(this@LauncherActivity, LoginActivity::class.java), REQUEST_SIGN)
        }

        register_button.onClick {
            startActivityForResult(Intent(this@LauncherActivity, RegisterActivity::class.java), REQUEST_SIGN)
        }

        remember_password_button.onClick {
            val endpoint = ConfigurationWrapper(this@LauncherActivity).endpoint.replace("/api/v2", "")
            val href = "$endpoint/web/password_reset/"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(href))
            startActivity(browserIntent)
        }

        debug_mode_off_button.onClick {
            ConfigurationWrapper(this@LauncherActivity).endpoint = ConfigurationWrapper.defaultEndpoint
            debugClicksCount = 0
            endpoint_layout.visibility = View.GONE
            debug_mode_off_button.visibility = View.GONE
        }

        logo_image.onClick {
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
        launchSpecificActivity()
        debugClicksCount = 0
        endpoint_input.setText(ConfigurationWrapper(this).endpoint, TextView.BufferType.EDITABLE)
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
        }
    }
}
