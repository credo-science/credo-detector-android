package science.credo.mobiledetector.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.openid.appauth.*
import science.credo.mobiledetector.App
import science.credo.mobiledetector.R
import science.credo.mobiledetector.intro.IntroActivity
import science.credo.mobiledetector.main.MainActivity
import science.credo.mobiledetector.network.Config
import science.credo.mobiledetector.network.RestInterface
import science.credo.mobiledetector.utils.Prefs


class LoginActivity : AppCompatActivity() {

    val TAG = "LoginActivity"
    //        ToDo change
    val CLIENT_ID = "226751"
    val OK_CODE = 100

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)

        }
        private lateinit var authorizationService: AuthorizationService
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (requestCode == OK_CODE) {
            handleAuthorizationResponse(intentData)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        viewProgress.visibility = View.VISIBLE

        if (!Prefs.get(this, String::class.java, "showIntro").equals("False")) {
            startActivity(Intent(this, IntroActivity::class.java))
        }

        btSignIn.setOnClickListener {
            addFragment(SignInFragment.newInstance())
        }

        btRegister.setOnClickListener {
            addFragment(RegisterFragment.newInstance())
        }

        btSignInViaSciStarter.setOnClickListener {
            signInViaSciStarter()
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

    fun silentLoginViaCode(provider: String, authenticationCode: String) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            val result = RestInterface.loginViaCode(this@LoginActivity, provider, authenticationCode)
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

    private fun signInViaSciStarter() {
        authorizationService = AuthorizationService(this)

        AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(Config.SCI_STARTER_DISCOVERY_ENDPOINT_HOST)) { authorizationServiceConfiguration, authorizationException ->
            if (authorizationServiceConfiguration != null) {
                val authorizationRequest = AuthorizationRequest.Builder(
                        authorizationServiceConfiguration,
                        CLIENT_ID,
                        ResponseTypeValues.CODE,
                        Uri.parse(Config.SCI_STARTER_REDIRECT_URI)
                    )
                    .setScope(Config.SCI_STARTER_SCOPE)
                    .build()
                val intent =
                    authorizationService.getAuthorizationRequestIntent(authorizationRequest)

                startActivityForResult(intent, OK_CODE)
            } else {
                if (authorizationException != null) {
                    val m = Throwable().stackTrace[0]
                    Log.e(TAG, "Exception " + authorizationException.toString())
                }
                //                    ToDo some failed message with authorizationException
            }

        }
    }


    private fun handleAuthorizationResponse(intentData: Intent?) {
        if (intentData == null) {
            val stackTraceElement = Throwable().stackTrace[0]
            Log.e(TAG, "Error intent call" + stackTraceElement.toString())
            return
        }
        val authorizationResponse = AuthorizationResponse.fromIntent(intentData)

        if (authorizationResponse != null) {
            authorizationResponse.authorizationCode?.let { silentLoginViaCode("scistarter", it) }
        }
    }
//    private fun handleAuthorizationResponse(intentData: Intent?) {
//        if (intentData == null) {
//            val stackTraceElement = Throwable().stackTrace[0]
//            Log.e(TAG, "Error intent call" + stackTraceElement.toString())
//            return
//        }
//
//        val authorizationResponse = AuthorizationResponse.fromIntent(intentData)
//        val authorizationException = AuthorizationException.fromIntent(intentData)
//
//        if (authorizationException != null || authorizationResponse == null) {
//            val stackTraceElement = Throwable().stackTrace[0]
//            Log.e(TAG, "Exception " + authorizationException.toString() + " stackTrace " + stackTraceElement.toString())
//            //                    ToDo some failed message with authorizationException
//            return
//        }
//
//        startCodeExchange(authorizationResponse)
//    }
//
//    private fun startCodeExchange(authorizationResponse: AuthorizationResponse){
//
//
//        authorizationService.performTokenRequest(
//            authorizationResponse.createTokenExchangeRequest()
//        ) { tokenResponse2, authorizationException2 ->
//            if (tokenResponse2 == null) {
//                Log.e(TAG, "Exception " + authorizationException2.toString())
//                //         ToDo msg for user whenAuthorizationFails(authorizationException2)
//            } else{
//                startActivity(MainActivity.intent(this@LoginActivity))
//            }
//        }
//    }
    override fun onDestroy() {
        super.onDestroy()
        authorizationService.dispose()
    }


//v    discovery
//v    app wysyla requesta z loginem i haslem do SciStarter. i redirect_url (musi trafic na whiteliste)
//    On uwierzytelnia, tworzy id_token, przekierowuje do Serwera Credo (on odpytuje tokenem SciStarter)
//    Serwer odsyla token CREDOWY do aplikacji
//        1. Ok
//        2. Konto stworzone, ale defult. Wyswietlamy formularz do zmiany danych request na API_URL /info
//    ID_TOKEN jest zapisany na serwerze i bedzie uzywany do dodawania punktow
//    Inny adres email: Jesli user chce powiazac stare konto do SciStartera to kontaktuj
}