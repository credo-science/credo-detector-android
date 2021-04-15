package science.credo.mobiledetector

import android.os.Bundle
import android.app.Activity
import android.app.ProgressDialog
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_user.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.credo.mobiledetector.database.UserInfoWrapper
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.network.ServerInterface
import science.credo.mobiledetector.network.exceptions.ServerException
import science.credo.mobiledetector.network.messages.LoginByEmailRequest
import science.credo.mobiledetector.network.messages.LoginByUsernameRequest
import science.credo.mobiledetector.network.messages.UserInfoRequest
import java.util.*
import java.util.concurrent.Future

class UserActivity : Activity() {

    private var saveTask: Future<Unit>? = null
    private var isClosed = false
    private var info : IdentityInfo.IdentityData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        info = IdentityInfo.getDefault(this).getIdentityData()

        actionBar?.setDisplayHomeAsUpEnabled(true)
        val uiw = UserInfoWrapper(this)
        display_name_input.setText(uiw.displayName)
        team_input.setText(uiw.team)
        //email_input.setText(uiw.email)

        save_button.setOnClickListener {
            save()
        }
    }

    override fun onDestroy() {
        saveTask?.cancel(true)
        isClosed = true
        super.onDestroy()
    }

    private fun save() {
        if (!validate()) {
            onSaveFailed(getString(R.string.user_message_validation_failed))
            return
        }

        save_button.isEnabled = false

        // TODO: https://stackoverflow.com/questions/45373007/progressdialog-is-deprecated-what-is-the-alternate-one-to-use
        val progressDialog = ProgressDialog(this,
                R.style.Theme_AppCompat_DayNight_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getText(R.string.user_message_save_pending))
        progressDialog.show()

        val saveRequest = UserInfoRequest.build(
                display_name_input.text.toString(),
                team_input.text.toString(),
                Locale.getDefault().language, //language_input.text.toString()
                info!!
        )

        val pref = UserInfoWrapper(this)

        saveTask = doAsync{
            try {

                val response = ServerInterface.getDefault(baseContext).info(saveRequest)
                saveTask = null
                uiThread {
                    if (!isClosed) {
                        pref.login = response.username
                        pref.email = response.email
                        pref.team = response.team
                        pref.displayName = response.display_name

                        progressDialog.dismiss()
                        onSaveSuccess()
                    }
                }
            } catch (e: ServerException) {
                // TODO: support response from server i.e. login/email already exists, no connection etc.
                uiThread {
                    if (!isClosed) {
                        progressDialog.dismiss()
                        onSaveFailed(e.error)
                    }
                }
            } catch (e: Exception) {
                uiThread {
                    if (!isClosed) {
                        progressDialog.dismiss()
                        onSaveFailed(e.message)
                    }
                }
            }
        }
    }

    private fun onSaveSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onSaveFailed(message: String?) {
        Toast.makeText(baseContext, message ?: getText(R.string.user_toast_save_failed), Toast.LENGTH_LONG).show()
        save_button.isEnabled = true
        display_name_input.error = message
    }

    private fun validate(): Boolean {
        var valid = true
        display_name_input.error = null
        return valid
    }
}
