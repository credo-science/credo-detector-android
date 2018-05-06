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

class LoginActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val btn_login = findViewById<Button>(R.id.login_button)
        val btn_link_singup = findViewById<TextView>(R.id.link_signup)
        val btn_forgot = findViewById<Button>(R.id.forgot_password_button)

        // Login
        btn_login!!.setOnClickListener { login() }

        // Go to SignupActivity
        btn_link_singup!!.setOnClickListener {
            val intent = Intent(applicationContext, SignupActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
        }

        btn_forgot!!.setOnClickListener{
            val intent = Intent(applicationContext, ResetPasswordActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
        }
    }

    fun login() {
        Log.d(TAG, "Login")

        if (!validate()) {
            onLoginFailed()
            return
        }

        val btn_login = findViewById<Button>(R.id.login_button)
        btn_login!!.isEnabled = false

        val progressDialog = ProgressDialog(this@LoginActivity,
                R.style.Theme_AppCompat_DayNight_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating...")
        progressDialog.show()

        // TODO: Implement your own authentication logic here.

        android.os.Handler().postDelayed(
                {
                    // On complete call either onLoginSuccess or onLoginFailed
                    onLoginSuccess()
                    // onLoginFailed();
                    progressDialog.dismiss()
                }, 3000)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == Activity.RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish()
            }
        }
    }

    override fun onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true)
    }

    fun onLoginSuccess() {
        val btn_login = findViewById<Button>(R.id.login_button)
        btn_login!!.isEnabled = true

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
        finish()
    }

    fun onLoginFailed() {
        Toast.makeText(baseContext, "Login failed", Toast.LENGTH_LONG).show()
        val btn_login = findViewById<Button>(R.id.login_button)
        btn_login!!.isEnabled = true
    }

    fun validate(): Boolean {
        var valid = true

        val email = findViewById<EditText>(R.id.input_email)
        val password = findViewById<EditText>(R.id.input_password)

        val emailStr = email!!.text.toString()
        val passwordStr = password!!.text.toString()

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email!!.error = "enter a valid email address"
            valid = false
        } else {
            email!!.error = null
        }

        if (passwordStr.isEmpty() || password.length() < 4 || password.length() > 10) {
            password!!.error = "between 4 and 10 alphanumeric characters"
            valid = false
        } else {
            password!!.error = null
        }

        return valid
    }

    companion object {
        private val TAG = "LoginActivity"
        private val REQUEST_SIGNUP = 0
    }
}
