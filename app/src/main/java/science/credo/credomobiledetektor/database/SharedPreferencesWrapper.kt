package science.credo.credomobiledetektor.database

import android.content.Context
import android.preference.PreferenceManager

open class SharedPreferencesWrapper(val context: Context) {
    protected val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

    fun setString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }
}
