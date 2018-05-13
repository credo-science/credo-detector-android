package science.credo.credomobiledetektor.database

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

    companion object {
        val defaultEndpoint = "https://api.credo.science/api/v2"
    }
}
