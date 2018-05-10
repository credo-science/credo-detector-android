package science.credo.credomobiledetektor

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import science.credo.credomobiledetektor.database.UserInfoWrapper
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.exceptions.ServerException
import science.credo.credomobiledetektor.network.messages.LoginByUsernameRequest
import science.credo.credomobiledetektor.network.messages.RegisterRequest
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SignupActivity : AppCompatActivity() {

    private var registerTask: Future<Unit>? = null
    private var isClosed = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        //Signup button
        signup_button.setOnClickListener({
            signup()
        })

        // Finish the registration screen and return to the Login activity
        link_login.setOnClickListener({
            finish()
        })
    }

    override fun onDestroy() {
        isClosed = true
        registerTask?.cancel(true)
        super.onDestroy()
    }

    fun signup() {
        Log.d(TAG, "Signup")

        if (!validate()) {
            onSignupFailed("Validation failed")
            return
        }

        signup_button.setEnabled(false)

        // TODO: use i.e. button with progress bar instead progress dialog
        val progressDialog = ProgressDialog(this@SignupActivity,
                R.style.Base_Theme_AppCompat_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        val registerRequest = RegisterRequest(
                input_email.text.toString(),
                input_name.text.toString(),
                input_name.text.toString(), // TODO: display name?
                input_password.text.toString(),
                "noteam", // TODO: field
                "pl",
                IdentityInfo.getInstance(this).getIdentityData()
        )

        val loginRequest = LoginByUsernameRequest(
                input_name.text.toString(),
                input_password.text.toString(),
                IdentityInfo.getInstance(this).getIdentityData()
        )

        val pref = UserInfoWrapper(this)

        registerTask = doAsync {
            try {
                ServerInterface.getDefault(baseContext).register(registerRequest)
                val response = ServerInterface.getDefault(baseContext).login(loginRequest)

                uiThread {
                    if (!isClosed) {
                        pref.login = response.username
                        pref.email = response.email
                        pref.token = response.token
                        pref.team = response.team

                        progressDialog.dismiss()
                        onSignupSuccess()
                    }
                }
            } catch (e: ServerException) {
                uiThread {
                    if (!isClosed) {
                        // TODO: support response from server i.e. login/email already exists, no connection etc.
                        progressDialog.dismiss()
                        onSignupFailed(e.error)
                    }
                }
            } catch (e :Exception) {
                uiThread {
                    if (!isClosed) {
                        progressDialog.dismiss()
                        onSignupFailed(e.message)
                    }
                }
            }
            registerTask = null
        }
    }

    private fun onSignupSuccess() {
        val btn_signup = findViewById<Button>(R.id.signup_button)
        btn_signup.setEnabled(true)
        setResult(Activity.RESULT_OK, null)
        finish()
    }

    private fun onSignupFailed(message:String?) {
        Toast.makeText(baseContext, "Login failed: $message", Toast.LENGTH_LONG).show()
        val btn_signup = findViewById<Button>(R.id.signup_button)
        btn_signup.setEnabled(true)
        input_email.setError(message)
    }

    fun validate(): Boolean {
        var valid = true

        //val name = findViewById<EditText>(R.id.input_name)
        //val email = findViewById<EditText>(R.id.input_email)
        //val password = findViewById<EditText>(R.id.input_password)

        val nameStr = input_name.getText().toString()
        val emailStr = input_email.getText().toString()
        val passwordStr = input_password.getText().toString()
        val password2Str = input_password2.getText().toString()

        if (nameStr.isEmpty() || input_name.length() < 3) {
            input_name.setError("at least 3 characters")
            valid = false
        } else {
            input_name.setError(null)
        }

        if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            input_email.setError("enter a valid email address")
            valid = false
        } else {
            input_email.setError(null)
        }

        if (passwordStr.isEmpty() || input_password.length() < 4 || input_password.length() > 10) {
            input_password.setError("between 4 and 10 alphanumeric characters")
            valid = false
        } else {
            input_password.setError(null)
        }

        if (passwordStr != password2Str) {
            input_password2.setError("passwords not match")
            valid = false
        } else {
            input_password2.setError(null)
        }

        return valid
    }

    companion object {
        private val TAG = "SignupActivity"
    }
}
