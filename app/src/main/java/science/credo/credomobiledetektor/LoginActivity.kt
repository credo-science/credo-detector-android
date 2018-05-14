package science.credo.credomobiledetektor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.credo.credomobiledetektor.database.UserInfoWrapper
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.exceptions.ServerException
import science.credo.credomobiledetektor.network.messages.LoginByEmailRequest
import science.credo.credomobiledetektor.network.messages.LoginByUsernameRequest
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
            startActivityForResult(Intent(applicationContext, ResetPasswordActivity::class.java), REQUEST_SIGNUP)
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
            onLoginFailed(getString(R.string.login_message_validation_failed))
            return
        }

        login_button.isEnabled = false

        // TODO: https://stackoverflow.com/questions/45373007/progressdialog-is-deprecated-what-is-the-alternate-one-to-use
        val progressDialog = ProgressDialog(this@LoginActivity,
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

                        progressDialog.dismiss()
                        onLoginSuccess()
                    }
                }
            } catch (e: ServerException) {
                // TODO: support response from server i.e. login/email already exists, no connection etc.
                uiThread {
                    if (!isClosed) {
                        progressDialog.dismiss()
                        onLoginFailed(e.error)
                    }
                }
            } catch (e: Exception) {
                uiThread {
                    if (!isClosed) {
                        progressDialog.dismiss()
                        onLoginFailed(e.message)
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

    private fun onLoginFailed(message: String?) {
        Toast.makeText(baseContext, message ?: getText(R.string.login_toast_login_failed), Toast.LENGTH_LONG).show()
        login_button.isEnabled = true
        email_input.error = message
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
            }
        } else {
            email_input.error = null
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
