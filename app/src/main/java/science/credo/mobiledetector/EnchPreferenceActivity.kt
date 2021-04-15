package science.credo.mobiledetector

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.MultiSelectListPreference
import android.util.Log
import android.view.MenuItem
import android.preference.*

/**
 * Created by poznan on 19/09/2017.
 *
 */

open class EnchPreferenceActivity : AppCompatPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var initPass = true
    val summary: HashMap<Preference, String> = HashMap<Preference, String>()
    open val TAG = "EnchPreferenceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
        Log.d(TAG, "onSharedPreferenceChanged: key: $key")
        val pref = findPreference(key)
        if (pref != null) updatePrefSummary(pref, sharedPreferences, key)
    }

    protected fun initSummary(p: Preference) {
        initPass = true
        if (p is PreferenceGroup) {
            val pGrp = p as PreferenceGroup
            for (i in 0..pGrp.getPreferenceCount() - 1) {
                initSummary(pGrp.getPreference(i))
            }
        } else {
            updatePrefSummary(p)
        }
        initPass = false
    }

    protected fun updatePrefSummary(p: Preference) {
        if (p is ListPreference) {
            val listPref = p as ListPreference
            p.setSummary(listPref.getEntry())
        }
        if (p is EditTextPreference) {

            Log.d(TAG, "updatePrefSummary ${p.key} " + p.title.toString() + " : " + (p as EditTextPreference).text )
            val editTextPref = p as EditTextPreference
            if (initPass) summary[p] = p.summary.toString()
            if (editTextPref.text != null && editTextPref.text.length > 0) {
                if (p.getTitle().toString().toLowerCase().contains("password")) {
                    p.setSummary("******")
                } else {
                    p.setSummary(editTextPref.getText())
                }
            } else {
                p.setSummary(summary[p])
            }
        }
        if (p is MultiSelectListPreference) {
            val editTextPref = p as EditTextPreference
            p.setSummary(editTextPref.getText())
        }
    }

    protected fun updatePrefSummary(p: Preference, sharedPreferences: SharedPreferences,
                                    key: String) {



        if (p is ListPreference) {
            val listPref = p as ListPreference
            p.setSummary(listPref.getEntry())
        }
        if (p is EditTextPreference) {
            val v = sharedPreferences.getString(key,"")!!
            Log.d(TAG, "updatePrefSummary ${p.key} " + p.title.toString() + " : " + (p as EditTextPreference).text )
            val editTextPref = p as EditTextPreference
            if (initPass) summary[p] = p.summary.toString()
            if (v.length > 0) {
                if (p.getTitle().toString().toLowerCase().contains("password")) {
                    p.setSummary("******")
                    p.text = v
                } else {
                    p.setSummary(v)
                    p.text = v
                }
            } else {
                p.setSummary(v)
                p.text = v
            }
        }
        if (p is MultiSelectListPreference) {
            val v = sharedPreferences.getString(key,"")
            val editTextPref = p as EditTextPreference
            p.setSummary(v)
            p.text = v
        }
    }

    override fun onResume() {
        super.onResume()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
    }


}