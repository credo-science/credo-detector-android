package science.credo.mobiledetector

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import science.credo.mobiledetector.databinding.ActivityRegisterBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.credo.mobiledetector.database.UserInfoWrapper
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.network.ServerInterface
import science.credo.mobiledetector.network.exceptions.ServerException
import science.credo.mobiledetector.network.messages.LoginByUsernameRequest
import science.credo.mobiledetector.network.messages.RegisterDeviceInfoRequest
import java.util.*
import java.util.concurrent.Future

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private var registerTask: Future<Unit>? = null
    private var isClosed = false
    private var info : IdentityInfo.IdentityData? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        info = IdentityInfo.getDefault(this@RegisterActivity).getIdentityData()

        binding.signupButton.setOnClickListener {
            signup()
        }

        binding.viewRulesButton.setOnClickListener {
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

        binding.signupButton.isEnabled = false

        // TODO: use i.e. button with progress bar instead progress dialog
        val progressDialog = ProgressDialog(this,
                R.style.Theme_AppCompat_DayNight_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getText(R.string.register_message_register_pending))
        progressDialog.show()

        val displayName = if (binding.displayNameInput.text.isBlank()) {
            binding.nameInput.text.toString()
        } else {
            binding.displayNameInput.text.toString()
        }

        val registerRequest = RegisterDeviceInfoRequest.build(
            binding.emailInput.text.toString(),
            binding.nameInput.text.toString(),
                displayName,
            binding.passwordInput.text.toString(),
            binding.teamInput.text.toString(),
                Locale.getDefault().language, //language_input.text.toString()
                info!!
        )

        val loginRequest = LoginByUsernameRequest.build(
            binding.nameInput.text.toString(),
            binding.passwordInput.text.toString(),
                info!!
        )

        val pref = UserInfoWrapper(this)

        registerTask = doAsync {
            var registered = false
            try {
                ServerInterface.getDefault(baseContext).register(registerRequest)
                registered = true
                pref.email = registerRequest.email
                pref.password = registerRequest.password

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
                        if (registered) {
                            onNeedActivate()
                        } else {
                            progressDialog.dismiss()
                            onSignupFailed(e.error)
                        }
                    }
                }
            } catch (e: Exception) {
                uiThread {
                    if (!isClosed) {
                        if (registered) {
                            onNeedActivate()
                        } else {
                            progressDialog.dismiss()
                            onSignupFailed(e.message)
                        }
                    }
                }
            }
            registerTask = null
        }
    }

    private fun onNeedActivate() {
        setResult(Activity.RESULT_FIRST_USER, null)
        finish()
    }

    private fun onSignupSuccess() {
        binding.signupButton.isEnabled = true
        setResult(Activity.RESULT_OK, null)
        finish()
    }

    private fun onSignupFailed(message:String?) {
        Toast.makeText(baseContext, message ?: getText(R.string.register_toast_register_failed), Toast.LENGTH_LONG).show()
        binding.signupButton.isEnabled = true
        binding.nameInput.error = message
        binding.emailInput.error = message
    }

    private fun validate(): Boolean {
        var valid = true

        val nameStr = binding.nameInput.text.toString()
        val emailStr = binding.emailInput.text.toString()
        val passwordStr = binding.passwordInput.text.toString()
        val password2Str = binding.password2Input.text.toString()

        if (nameStr.isEmpty() || binding.nameInput.length() < 3) {
            binding.nameInput.error = getText(R.string.register_input_login_validation)
            valid = false
        } else {
            binding.nameInput.error = null
        }

        if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            binding.emailInput.error = getText(R.string.register_input_email_validation)
            valid = false
        } else {
            binding.emailInput.error = null
        }

        if (passwordStr.isEmpty() || binding.passwordInput.length() < 4) {
            binding.passwordInput.error = getText(R.string.register_input_password_validation)
            valid = false
        } else {
            binding.passwordInput.error = null
        }

        if (passwordStr != password2Str) {
            binding.password2Input.error = getText(R.string.register_input_password2_validation)
            valid = false
        } else {
            binding.password2Input.error = null
        }

        if (!binding.acceptRulesInput.isChecked) {
            binding.acceptRulesInput.error = getText(R.string.register_input_accept_rules_validation)
            valid = false
        } else {
            binding.acceptRulesInput.error = null
        }

        return valid
    }

    companion object {
        private val TAG = "RegisterActivity"
    }
}
