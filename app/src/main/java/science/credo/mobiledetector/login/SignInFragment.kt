package science.credo.mobiledetector.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import science.credo.mobiledetector.main.MainActivity
import science.credo.mobiledetector.R
import science.credo.mobiledetector.network.RestInterface
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.utils.UiUtils

class SignInFragment private constructor() : Fragment() {

    companion object {

        fun newInstance(): SignInFragment {
            val instance = SignInFragment()
            return instance
        }

    }


    lateinit var etPassword: EditText
    lateinit var btSignIn: TextView
    lateinit var btForgotPassword: TextView
    lateinit var etLogin: EditText
    lateinit var viewProgress : View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_sign_in, container, false)
        etPassword = v.findViewById(R.id.etPassword)
        etLogin = v.findViewById(R.id.etLogin)
        btForgotPassword = v.findViewById(R.id.btForgotPassword)
        btSignIn = v.findViewById(R.id.btSignIn)
        viewProgress = v.findViewById(R.id.viewProgress)

        etPassword.setOnEditorActionListener { v, actionId, event ->
            val b = actionId == EditorInfo.IME_ACTION_DONE
            if (b) {
                btSignIn.performClick()
            }
            return@setOnEditorActionListener b
        }

        btSignIn.setOnClickListener {
            UiUtils.hideSoftKeyboard(activity!!)
            signIn(
                etLogin.text.toString(),
                etPassword.text.toString()
            )
        }

        btForgotPassword.setOnClickListener {
            //            addFragment(ForgotPasswordFragment.newInstance(email))
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    fun signIn(login: String, password: String) {

        GlobalScope.launch (Dispatchers.Main) {
            viewProgress.visibility = View.VISIBLE
            val result = RestInterface.login(context!!, login, password)
            viewProgress.visibility = View.GONE
            if(result.isSuccess()){
                Prefs.put(context!!, login,Prefs.Keys.USER_LOGIN)
                Prefs.put(context!!, password,Prefs.Keys.USER_PASSWORD)
                startActivity(MainActivity.intent(context!!))
            }else{
//                startActivity(MainActivity.intent(context!!))
                UiUtils.showAlertDialog(context!!,result.getResponse())
            }
        }

    }
}