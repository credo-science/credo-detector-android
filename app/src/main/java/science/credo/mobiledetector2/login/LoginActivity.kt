package science.credo.mobiledetector2.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import science.credo.mobiledetector2.App
import science.credo.mobiledetector2.R
import science.credo.mobiledetector2.intro.IntroActivity
import science.credo.mobiledetector2.main.MainActivity
import science.credo.mobiledetector2.network.RestInterface
import science.credo.mobiledetector2.utils.Prefs

class LoginActivity : AppCompatActivity() {

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        viewProgress.visibility = View.VISIBLE

        if (!Prefs.get(this, String::class.java, "showIntro").equals("False")) {
            startActivity(Intent(this, IntroActivity::class.java))
        }

        btRegister.setOnClickListener {
            addFragment(RegisterFragment.newInstance())
        }

        btSignIn.setOnClickListener {
            addFragment(SignInFragment.newInstance())
//            if (Prefs.get(this, OldCameraSettings::class.java) == null) {
//                startActivity(SettingsActivity.intent(this, true))
//            } else {
//                startActivity(SettingsActivity.intent(this))
//            }
        }

        val savedLogin = Prefs.get(this, String::class.java, Prefs.Keys.USER_LOGIN)
        val savedPassword = Prefs.get(this, String::class.java, Prefs.Keys.USER_PASSWORD)
        if (savedLogin != null && savedPassword != null) {
            silentLogin(savedLogin, savedPassword)
        } else {
            containerButtons.visibility = View.VISIBLE
            viewProgress.visibility = View.GONE
        }


    }

    fun silentLogin(login: String, password: String) {

        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            val result = RestInterface.login(this@LoginActivity, login, password)
            if (result.isSuccess()) {
                viewProgress.visibility = View.GONE
                App.token = result.getCastedResponse(LoginResponse::class.java)?.token?:""
                Prefs.put(this@LoginActivity,App.token,Prefs.Keys.USER_TOKEN)
                startActivity(MainActivity.intent(this@LoginActivity))
                finish()
            } else {
                containerButtons.visibility = View.VISIBLE
                viewProgress.visibility = View.GONE
            }
        }

    }

    fun addFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.containerFragments, fragment)
        transaction.addToBackStack(fragment::class.java.simpleName)
        transaction.commit()
    }

}