package science.credo.credomobiledetektor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.uiThread
import science.credo.credomobiledetektor.database.UserInfoWrapper
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.exceptions.ServerException
import science.credo.credomobiledetektor.network.messages.LoginByUsernameRequest
import science.credo.credomobiledetektor.network.messages.RegisterDeviceInfoRequest
import java.util.*
import java.util.concurrent.Future

class RegisterActivity : AppCompatActivity() {

    private var registerTask: Future<Unit>? = null
    private var isClosed = false
    private var info : IdentityInfo.IdentityData? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        info = IdentityInfo.getDefault(this@RegisterActivity).getIdentityData()

        signup_button.onClick{
            signup()
        }

        view_rules_button.onClick {
            startActivity(Intent(this@RegisterActivity, RulesActivity::class.java))
        }

        //language_input.setText(Locale.getDefault().language)
    }

    override fun onDestroy() {
        isClosed = true
        registerTask?.cancel(true)
        super.onDestroy()
    }

    private fun signup() {
        Log.d(TAG, "Signup")

        if (!validate()) {
            onSignupFailed(getString(R.string.register_message_validation_failed))
            return
        }

        signup_button.setEnabled(false)

        // TODO: use i.e. button with progress bar instead progress dialog
        val progressDialog = ProgressDialog(this@RegisterActivity,
                R.style.Base_Theme_AppCompat_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getString(R.string.register_message_register_pending))
        progressDialog.show()

        val registerRequest = RegisterDeviceInfoRequest.build(
                email_input.text.toString(),
                name_input.text.toString(),
                display_name_input.text.toString(),
                password_input.text.toString(),
                team_input.text.toString(),
                Locale.getDefault().language, //language_input.text.toString()
                info!!
        )

        val loginRequest = LoginByUsernameRequest.build(
                name_input.text.toString(),
                password_input.text.toString(),
                info!!
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
                        pref.displayName = response.display_name ?: response.username

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
        Toast.makeText(baseContext, message ?: getText(R.string.register_toast_register_failed), Toast.LENGTH_LONG).show()
        val btn_signup = findViewById<Button>(R.id.signup_button)
        btn_signup.setEnabled(true)
        email_input.setError(message)
    }

    private fun validate(): Boolean {
        var valid = true

        val nameStr = name_input.text.toString()
        val emailStr = email_input.text.toString()
        val passwordStr = password_input.text.toString()
        val password2Str = password2_input.text.toString()

        if (nameStr.isEmpty() || name_input.length() < 3) {
            name_input.error = getText(R.string.register_input_login_validation)
            valid = false
        } else {
            name_input.error = null
        }

        if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email_input.error = getText(R.string.register_input_email_validation)
            valid = false
        } else {
            email_input.error = null
        }

        if (passwordStr.isEmpty() || password_input.length() < 4) {
            password_input.error = getText(R.string.register_input_password_validation)
            valid = false
        } else {
            password_input.error = null
        }

        if (passwordStr != password2Str) {
            password2_input.error = getText(R.string.register_input_password2_validation)
            valid = false
        } else {
            password2_input.error = null
        }

        if (!accept_rules_input.isChecked) {
            accept_rules_input.error = getText(R.string.register_input_accept_rules_validation)
            valid = false
        } else {
            accept_rules_input.error = null
        }

        return valid
    }

    companion object {
        private val TAG = "RegisterActivity"
    }
}
