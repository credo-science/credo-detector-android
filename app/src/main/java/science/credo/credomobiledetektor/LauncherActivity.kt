package science.credo.credomobiledetektor

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import science.credo.credomobiledetektor.database.UserInfoWrapper

const val REQUEST_MAIN = 1
const val REQUEST_SIGN = 2

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
    }

    override fun onPostResume() {
        super.onPostResume()
        launchSpecificActivity()
    }

    private fun launchSpecificActivity() {
        val pref = UserInfoWrapper(this)
        val check = pref.token.isEmpty()

        if (check) {
            startActivityForResult(Intent(this, LoginActivity::class.java), REQUEST_SIGN)
        } else {
            startActivityForResult(Intent(this, MainActivity::class.java), REQUEST_MAIN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            finish()
        }
    }
}
