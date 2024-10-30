package science.credo.mobiledetector


import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.util.Log
import science.credo.mobiledetector.info.ConfigurationInfo


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

        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val prefHash = pref.all
        for (key in pref.all) {
            Log.d(TAG, "${key.toString()}")
        }

        val ca = application as CredoApplication
        val cs = ca.cameraSettings
        if (cs != null) {
            val listCameraNumber = findPreference("camera_number") as ListPreference
            val cameraEntries = arrayOfNulls<CharSequence>(cs.numberOfCameras)
            val cameraEntryValues = arrayOfNulls<CharSequence>(cs.numberOfCameras)
            for (i in 0 until cs.numberOfCameras){
                cameraEntryValues[i] = "$i"
                when (i) {
                    0 -> {
                        cameraEntries[i] = getString(R.string.pref_camera_number_entity_back)
                    }
                    1 -> {
                        cameraEntries[i] = getString(R.string.pref_camera_number_entity_front)
                    }
                    else -> {
                        cameraEntries[i] = getString(R.string.pref_camera_number_entity_other, i)
                    }
                }
            }
            listCameraNumber.entries = cameraEntries
            listCameraNumber.entryValues = cameraEntryValues
            listCameraNumber.setDefaultValue("0")

            updateFrameSizes()
        }

        initSummary(preferenceScreen)

        ConfigurationInfo(this).isDetectionOn = false
        (applicationContext as CredoApplication).turnOffDetection()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "camera_number") {
            updateFrameSizes()
        }
        super.onSharedPreferenceChanged(sharedPreferences, key)
    }

    private fun updateFrameSizes() {
        val ca = application as CredoApplication
        val cs = ca.cameraSettings
        if (cs != null) {

            val listCameraNumber = findPreference("camera_number") as ListPreference
            val cameraNumber =  listCameraNumber.value

            if (cameraNumber != null) {
                val sizes = cs.cameras[cameraNumber.toInt()].sizes
                val listFrameSize = findPreference("frame_size") as ListPreference
                val frameSizeEntries = arrayOfNulls<CharSequence>(sizes.size)
                val frameSizeValues = arrayOfNulls<CharSequence>(sizes.size)

                for (i in 0 until sizes.size) {
                    val w = sizes[i].width
                    val h = sizes[i].height
                    frameSizeEntries[i] = "$w x $h"
                    frameSizeValues[i] = "$i"
                }

                listFrameSize.entries = frameSizeEntries
                listFrameSize.entryValues = frameSizeValues
                listFrameSize.setDefaultValue("0")
            }
        }
    }
}
