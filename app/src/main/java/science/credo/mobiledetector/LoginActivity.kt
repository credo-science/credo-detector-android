package science.credo.mobiledetector

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.database.UserInfoWrapper
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.network.ServerInterface
import science.credo.mobiledetector.network.exceptions.ServerException
import science.credo.mobiledetector.network.messages.LoginByEmailRequest
import science.credo.mobiledetector.network.messages.LoginByUsernameRequest
import java.util.concurrent.Future

class LoginActivity : AppCompatActivity() {

    private var loginTask: Future<Unit>? = null
    private var isClosed = false
    private var info : IdentityInfo.IdentityData? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        info = IdentityInfo.getDefault(this).getIdentityData()

        if (!UserInfoWrapper(this).token.isEmpty()) {
            setResult(Activity.RESULT_OK)
            finish()
            return
        }

        // Login
        login_button.setOnClickListener { login() }

        // Go to Forgot password Activity
        remember_password_button.setOnClickListener {
            val endpoint = ConfigurationWrapper(this@LoginActivity).endpoint.replace("/api/v2", "")
            val href = "$endpoint/web/password_reset/"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(href))
            startActivity(browserIntent)
        }

        val pref = UserInfoWrapper(this)
        if (!pref.email.isBlank() && !pref.password.isBlank()) {
            email_input.setText(pref.email)
            password_input.setText(pref.password)
        }
    }

    override fun onDestroy() {
        loginTask?.cancel(true)
        isClosed = true
        super.onDestroy()
    }

    fun login() {
        Log.d(TAG, "Login")

        if (!validate()) {
            onLoginFailed(0, getString(R.string.login_message_validation_failed))
            return
        }

        login_button.isEnabled = false

        // TODO: https://stackoverflow.com/questions/45373007/progressdialog-is-deprecated-what-is-the-alternate-one-to-use
        val progressDialog = ProgressDialog(this,
                R.style.Theme_AppCompat_DayNight_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getText(R.string.login_message_login_pending))
        progressDialog.show()

        val inputText = email_input.text.toString()

        val loginRequest = if ('@' in inputText) {
            LoginByEmailRequest.build(
                    inputText,
                    password_input.text.toString(),
                    info!!
            )
        } else {
            LoginByUsernameRequest.build(
                    inputText,
                    password_input.text.toString(),
                    info!!
            )
        }

        val pref = UserInfoWrapper(this)

        loginTask = doAsync{
            try {

                val response = ServerInterface.getDefault(baseContext).login(loginRequest)
                loginTask = null
                uiThread {
                    if (!isClosed) {
                        pref.login = response.username
                        pref.email = response.email
                        pref.token = response.token
                        pref.team = response.team
                        pref.displayName = response.display_name

                        progressDialog.dismiss()
                        onLoginSuccess()
                    }
                }
            } catch (e: ServerException) {
                Log.e(TAG, "Server error: ", e)
                // TODO: support response from server i.e. login/email already exists, no connection etc.
                uiThread {
                    if (!isClosed) {
                        progressDialog.dismiss()
                        onLoginFailed(e.code, e.error)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Other error: ", e)
                uiThread {
                    if (!isClosed) {
                        progressDialog.dismiss()
                        onLoginFailed(-1, e.message)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK)
                this.finish()
            }
        }
    }

    private fun onLoginSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onLoginFailed(code: Int, message: String?) {
        val msg = translateMessage(baseContext, message, getText(R.string.login_toast_login_failed));

        Toast.makeText(baseContext, msg ?: getText(R.string.login_toast_login_failed), Toast.LENGTH_LONG).show()
        login_button.isEnabled = true
        error_text.text = msg
    }

    private fun validate(): Boolean {
        var valid = true

        val emailStr = email_input.text.toString()
        val passwordStr = password_input.text.toString()

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            if (emailStr.isEmpty() || email_input.length() < 3) {
                email_input.error = getText(R.string.login_input_login_validation)
                valid = false
            } else {
                email_input.setError(null)
                error_text.text = ""
            }
        } else {
            email_input.error = null
            error_text.text = ""
        }

        if (passwordStr.isEmpty() || password_input.length() < 4) {
            password_input.error = getText(R.string.login_input_password_validation)
            valid = false
        } else {
            password_input.error = null
        }

        return valid
    }

    companion object {
        private val TAG = "LoginActivity"
        private val REQUEST_SIGNUP = 0
    }
}
