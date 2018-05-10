package science.credo.credomobiledetektor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import science.credo.credomobiledetektor.database.UserInfoWrapper
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.exceptions.ServerException
import science.credo.credomobiledetektor.network.messages.LoginByEmailRequest
import science.credo.credomobiledetektor.network.messages.LoginByUsernameRequest
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private var loginTask: Future<Unit>? = null
    private var isClosed = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (!UserInfoWrapper(this).token.isEmpty()) {
            setResult(Activity.RESULT_OK)
            finish()
            return
        }

        // Login
        login_button.setOnClickListener { login() }

        // Go to Forgot password Activity
        remember_password_link.setOnClickListener {
            val intent = Intent(applicationContext, ResetPasswordActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
        }

        // Go to SignupActivity
        singup_button.setOnClickListener{
            val intent = Intent(applicationContext, SignupActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
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
            onLoginFailed("Validation failed")
            return
        }

        login_button.isEnabled = false

        // TODO: https://stackoverflow.com/questions/45373007/progressdialog-is-deprecated-what-is-the-alternate-one-to-use
        val progressDialog = ProgressDialog(this@LoginActivity,
                R.style.Theme_AppCompat_DayNight_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Authenticating...")
        progressDialog.show()

        val inputText = input_email.text.toString()

        val loginRequest = if ('@' in inputText) {
            LoginByEmailRequest(
                    inputText,
                    input_password.text.toString(),
                    IdentityInfo.getInstance(applicationContext).getIdentityData()
            )
        } else {
            LoginByUsernameRequest(
                    inputText,
                    input_password.text.toString(),
                    IdentityInfo.getInstance(applicationContext).getIdentityData()
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

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                setResult(Activity.RESULT_OK)
                this.finish()
            }
        }
    }

    override fun onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true)
    }

    fun onLoginSuccess() {
        /*login_button.isEnabled = true

        // Set Token to not null
        val check = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .edit()
                .putString("LOGIN_TOKEN", "Successful")
                .apply()

        var activityIntent = Intent()
        if (check != null) {
            activityIntent =  Intent(this, MainActivity::class.java)
        } else {
            activityIntent =  Intent(this, LoginActivity::class.java)
        }
        startActivity(activityIntent);
        finish()*/
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun onLoginFailed(message: String?) {
        Toast.makeText(baseContext, "Login failed: $message", Toast.LENGTH_LONG).show()
        login_button.isEnabled = true
        input_email.error = message
    }

    fun validate(): Boolean {
        var valid = true

        val emailStr = input_email.text.toString()
        val passwordStr = input_password.text.toString()

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            if (emailStr.isEmpty() || input_email.length() < 3) {
                input_email.setError("at least 3 characters or enter a valid email address")
                valid = false
            } else {
                input_email.setError(null)
            }
        } else {
            input_email.error = null
        }

        if (passwordStr.isEmpty() || input_password.length() < 4 || input_password.length() > 10) {
            input_password.error = "between 4 and 10 alphanumeric characters"
            valid = false
        } else {
            input_password.error = null
        }

        return valid
    }

    companion object {
        private val TAG = "LoginActivity"
        private val REQUEST_SIGNUP = 0
    }
}
