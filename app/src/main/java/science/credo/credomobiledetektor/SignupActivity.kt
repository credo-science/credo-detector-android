package science.credo.credomobiledetektor

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class SignupActivity : AppCompatActivity() {


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btn_signup = findViewById<Button>(R.id.signup_button)
        val btn_link_login = findViewById<TextView>(R.id.link_login)

        //Signup button
        btn_signup.setOnClickListener(View.OnClickListener {
            signup()
        })

        // Finish the registration screen and return to the Login activity
        btn_link_login.setOnClickListener(View.OnClickListener {
            finish()
        })
    }

    fun signup() {
        Log.d(TAG, "Signup")

        if (!validate()) {
            onSignupFailed()
            return
        }

        val btn_signup = findViewById<Button>(R.id.signup_button)
        btn_signup.setEnabled(false)

        val progressDialog = ProgressDialog(this@SignupActivity,
                R.style.Base_Theme_AppCompat_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()


        // TODO: Implement signup logic here.

        android.os.Handler().postDelayed(
                {
                    // On complete call either onSignupSuccess or onSignupFailed
                    // depending on success
                    onSignupSuccess()
                    // onSignupFailed();
                    progressDialog.dismiss()
                }, 3000)
    }


    fun onSignupSuccess() {
        val btn_signup = findViewById<Button>(R.id.signup_button)
        btn_signup.setEnabled(true)
        setResult(Activity.RESULT_OK, null)
        finish()
    }

    fun onSignupFailed() {
        Toast.makeText(baseContext, "Login failed", Toast.LENGTH_LONG).show()
        val btn_signup = findViewById<Button>(R.id.signup_button)
        btn_signup.setEnabled(true)
    }

    fun validate(): Boolean {
        var valid = true

        val name = findViewById<EditText>(R.id.input_name)
        val email = findViewById<EditText>(R.id.input_email)
        val password = findViewById<EditText>(R.id.input_password)

        val nameStr = name.getText().toString()
        val emailStr = email.getText().toString()
        val passwordStr = password.getText().toString()

        if (nameStr.isEmpty() || name.length() < 3) {
            name.setError("at least 3 characters")
            valid = false
        } else {
            name.setError(null)
        }

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("enter a valid email address")
            valid = false
        } else {
            email.setError(null)
        }

        if (passwordStr.isEmpty() || password.length() < 4 || password.length() > 10) {
            password.setError("between 4 and 10 alphanumeric characters")
            valid = false
        } else {
            password.setError(null)
        }

        return valid
    }

    companion object {
        private val TAG = "SignupActivity"
    }
}
