package science.credo.credomobiledetektor.database

import android.content.Context
import android.preference.PreferenceManager

class UserInfoWrapper(context: Context) : SharedPreferencesWrapper(context) {
    var login : String
        get() {
            return preferences.getString("user_login", "")
        }
        set(v) {
            setString("user_login", v)
        }

    var email : String
        get() {
            return preferences.getString("user_email", "")
        }
        set(v) {
            setString("user_email", v)
        }

    var team : String
        get() {
            return preferences.getString("user_team", "")
        }
        set(v) {
            setString("user_team", v)
        }

    var token : String
        get() {
            return preferences.getString("user_token", "")
        }
        set(v) {
            setString("user_token", v)
        }
}
