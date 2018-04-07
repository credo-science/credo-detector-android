package science.credo.credomobiledetektor

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import science.credo.credomobiledetektor.info.ConfigurationInfo
import science.credo.credomobiledetektor.info.UserInfo
import science.credo.credomobiledetektor.network.NetworkInterface
import science.credo.credomobiledetektor.network.message.`in`.LoginFrame


/**
 * Created by poznan on 19/09/2017.
 */

class RegisterActivity : EnchPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override val TAG = "RegisterActivity"
    var mConfiguration: ConfigurationInfo? = null //ConfigurationInfo.getInstance(applicationContext)
    val mMapper = jacksonObjectMapper()

    fun onClick(v: View) {
        Log.d(TAG, "onClick: " +v)
        when (v.id) {
            R.id.login_button -> loginAction()
            R.id.register_button -> registerAction()
        }
    }


    fun loginAction() {
        if (mConfiguration!!.isConnected) {
            val userInfo = UserInfo.getNewInstance(applicationContext)
            if (userInfo.key != "") {
                doAsync {
                    val result = NetworkInterface.getInstance(applicationContext).sendLogin()
                    when {
                        result.code == NetworkInterface.ok -> {
                            uiThread {
                                longToast("login action ok")
                                val loginFrame = mMapper.readValue(result.message, LoginFrame::class.java)
                                Log.d(TAG, "login: loginFrame: " + loginFrame?.body?.user_info?.email ?: "NULL")
                                Log.d(TAG, "login, email in preferences " +
                                PreferenceManager.getDefaultSharedPreferences(applicationContext).
                                        getString("user_email_address","NULL")
                                )

                                val ed = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                                ed.putString("user_email_address", loginFrame.body.user_info.email)
                                ed.putString("user_name", loginFrame.body.user_info.name)
                                ed.putString("user_team", loginFrame.body.user_info.team)
                                Log.d(TAG, "before commit")
                                ed.apply()
                                Log.d(TAG, "login, email in preferences after commit " +
                                        PreferenceManager.getDefaultSharedPreferences(applicationContext).
                                                getString("user_email_address","NULL")
                                )
                            }
                        }
                        result.code in 400..500 -> {
                            val msg = NetworkInterface.getInstance(applicationContext).getError(result.message)
                            val msgs = msg?.message ?: getString(R.string.msg_network_connection_problem)
                            uiThread {
                                longToast(msgs)
                            }
                        }
                        else -> {
                            uiThread {
                                longToast(getString(R.string.msg_network_connection_problem))
                            }
                        }
                    }
                }
            } else {
                longToast("please enter user key")
            }
        } else {
            longToast(getString(R.string.msg_no_network_connection))
        }
    }

    fun registerAction() {
        if (mConfiguration!!.isConnected) {
            val userInfo = UserInfo.getNewInstance(applicationContext)
            if (userInfo.email != "" && userInfo.name != "") {
                doAsync {
                    val result = NetworkInterface.getInstance(applicationContext).sendRegister()
                    when {
                        result.code == NetworkInterface.ok -> {
                            uiThread {
                                longToast("registration performed, please check your email")
                            }
                        }
                        (result.code >= 400 && result.code <= 500) -> {
                            val msg = NetworkInterface.getInstance(applicationContext).getError(result.message)
                            val msgs = msg?.message ?: getString(R.string.msg_network_connection_problem)
                            uiThread {
                                longToast(msgs)
                            }
                        }
                        else -> {
                            uiThread {
                                longToast(getString(R.string.msg_network_connection_problem))
                            }
                        }
                    }
                }
            } else {
                longToast("please enter: Name, E-mail and Password")
            }
        } else {
            longToast(getString(R.string.msg_no_network_connection))
        }
    }

    fun registerListeners() {
        val lButton = findViewById<Button>(R.id.login_button)
        val rButton = findViewById<Button>(R.id.register_button)

        Log.d(TAG, "registerListeners: login button" + lButton)
        Log.d(TAG, "registerListeners: register button" + rButton)

        lButton?.setOnClickListener {
            Log.d(TAG, "onClink login button clicked")
            loginAction()
        }
        rButton?.setOnClickListener {
            Log.d(TAG, "onClink register button clicked")
            registerAction()
        }
    }

    fun unregisterListeners() {
        val lButton = findViewById<Button>(R.id.login_button)
        val rButton = findViewById<Button>(R.id.register_button)
        lButton?.setOnClickListener(null)
        rButton?.setOnClickListener(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mConfiguration = ConfigurationInfo(applicationContext)
        Log.d(TAG, "onCreate")
        addPreferencesFromResource(R.xml.register)
        PreferenceManager.setDefaultValues(this, R.xml.register,
                false)
        setupActionBar()
        initSummary(preferenceScreen)
    }
}

