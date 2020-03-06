package science.credo.mobiledetector.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.network.RestInterface
import science.credo.mobiledetector.utils.UiUtils


class RegisterFragment private constructor() : Fragment(), Validator.ValidationListener {


    companion object {

        fun newInstance(): RegisterFragment {
            val instance = RegisterFragment()
            return instance
        }

    }

    @NotEmpty
    private lateinit var etLogin: EditText
    @Email
    private lateinit var etEmail: EditText
    @Password
    private lateinit var etPassword: EditText
    @ConfirmPassword
    private lateinit var etPasswordConfirm: EditText
    private lateinit var etDisplayName: EditText
    private lateinit var etTeam: EditText
    @Checked
    private lateinit var cbTerms: CheckBox
    private lateinit var btRegister: TextView
    private lateinit var viewProgress: View


    private lateinit var validatior: Validator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_register, container, false)
        etPasswordConfirm = v.findViewById(R.id.etPasswordConfirm)
        etDisplayName = v.findViewById(R.id.etDisplayName)
        etTeam = v.findViewById(R.id.etTeam)
        cbTerms = v.findViewById(R.id.cbTerms)
        viewProgress = v.findViewById(R.id.viewProgress)
        etLogin = v.findViewById(R.id.etLogin)
        etPassword = v.findViewById(R.id.etPassword)
        etEmail = v.findViewById(R.id.etEmail)
        btRegister = v.findViewById(R.id.btRegister)
        viewProgress = v.findViewById(R.id.viewProgress)

        etTeam.setOnEditorActionListener { v, actionId, event ->
            val b = actionId == EditorInfo.IME_ACTION_DONE
            if (b) {
                btRegister.performClick()
            }
            return@setOnEditorActionListener b
        }

        btRegister.setOnClickListener {
            UiUtils.hideSoftKeyboard(activity!!)
            validatior.validate()
        }

        validatior = Validator(this)
        validatior.setValidationListener(this)


        return v
    }


    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        GlobalScope.launch(Dispatchers.Main) {
            for (error in errors ?: emptyList<ValidationError>()) {
                if (error.view is EditText) {
                    (error.view as EditText).error = error.getCollatedErrorMessage(context!!)
                } else if (error.view is CheckBox) {
                    UiUtils.showAlertDialog(context!!, error.getCollatedErrorMessage(context!!))
                }
            }
        }
    }

    override fun onValidationSucceeded() {
        register()
    }


    fun register() {


        GlobalScope.launch(Dispatchers.Main) {
            viewProgress.visibility = View.VISIBLE
            val result = RestInterface.register(
                context!!,
                etLogin.text.toString(),
                etEmail.text.toString(),
                etPassword.text.toString(),
                if (etDisplayName.text.isNotEmpty()) etDisplayName.text.toString() else etLogin.text.toString(),
                etTeam.text.toString()
            )
            viewProgress.visibility = View.GONE
            if (result.isSuccess()) {
                activity?.finish()
            } else {
                UiUtils.showAlertDialog(context!!, result.getResponse())
            }
        }

    }
}