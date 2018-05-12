package science.credo.credomobiledetektor

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val check = PreferenceManager.getDefaultSharedPreferences(this).getString("LOGIN_TOKEN",  null)

        var activityIntent = Intent()

        if(check == null){
            activityIntent = Intent(this, StartActivity::class.java)
        }
        else{
            activityIntent = Intent(this, MainActivity::class.java)
        }
        startActivity(activityIntent)
        finish()
    }
}
