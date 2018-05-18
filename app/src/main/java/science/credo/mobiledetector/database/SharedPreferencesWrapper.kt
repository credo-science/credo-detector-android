package science.credo.mobiledetector.database

import android.content.Context
import android.preference.PreferenceManager

open class SharedPreferencesWrapper(val context: Context) {
    protected val preferences = PreferenceManager.getDefaultSharedPreferences(context)!!

    fun setString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun setFloat(key: String, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }

    fun setInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun setLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun setBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }
}
