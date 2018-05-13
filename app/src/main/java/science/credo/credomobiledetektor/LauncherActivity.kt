package science.credo.credomobiledetektor

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_launcher.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import science.credo.credomobiledetektor.database.ConfigurationWrapper
import science.credo.credomobiledetektor.database.UserInfoWrapper

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
            startActivityForResult(Intent(this@LauncherActivity, SignupActivity::class.java), REQUEST_SIGN)
        }

        remember_password_button.onClick {
            startActivityForResult(Intent(applicationContext, ResetPasswordActivity::class.java), REQUEST_SIGN)
        }

        endpoint_input.setText(ConfigurationWrapper(this).endpoint, TextView.BufferType.EDITABLE)

        logo_image.onClick {
            debugClicksCount++
            if (debugClicksCount >= debugClicksToActivate) {
                endpoint_layout.visibility = View.VISIBLE
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
