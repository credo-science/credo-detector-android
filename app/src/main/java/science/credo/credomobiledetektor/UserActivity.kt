package science.credo.credomobiledetektor

import android.os.Bundle
import android.app.Activity

import kotlinx.android.synthetic.main.activity_user.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import science.credo.credomobiledetektor.database.UserInfoWrapper

class UserActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val uiw = UserInfoWrapper(this)
        display_name_input.setText(uiw.displayName)
        team_input.setText(uiw.team)
        email_input.setText(uiw.email)

        save_button.onClick {
            // TODO save action
        }
    }

}
