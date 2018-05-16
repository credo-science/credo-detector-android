package science.credo.mobiledetector.database

import android.content.Context
import android.preference.PreferenceManager

class ConfigurationWrapper(context: Context) : SharedPreferencesWrapper(context) {
    var endpoint : String
        get() {
            return preferences.getString("endpoint", defaultEndpoint)
        }
        set(v) {
            setString("endpoint", v)
        }

    var dbSchema : String
        get() {
            return preferences.getString("db_schema", "")
        }
        set(v) {
            setString("db_schema", v)
        }

    companion object {
        val defaultEndpoint = "https://api.credo.science/api/v2"
    }
}
