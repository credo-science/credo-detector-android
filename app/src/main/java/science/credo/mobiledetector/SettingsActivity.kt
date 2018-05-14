package science.credo.mobiledetector


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import java.util.prefs.Preferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.preference.*


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */


class SettingsActivity : EnchPreferenceActivity() {

    override val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        addPreferencesFromResource(R.xml.preferences)
        PreferenceManager.setDefaultValues(this, R.xml.preferences,
                false)
        setupActionBar()
        initSummary(preferenceScreen)


        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val prefHash = pref.all
        for (key in pref.all) {
            Log.d(TAG, "${key.toString()}")
        }
    }
}
