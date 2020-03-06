package science.credo.mobiledetector.settings

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Camera
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.utils.UiUtils

class SettingsActivity : AppCompatActivity() {

    var firstRun: Boolean = false

    companion object {
        const val EXTRA_FIRST_RUN = "ex_first_run"
        fun intent(context: Context): Intent {
            return intent(context, false)
        }

        fun intent(context: Context, firstRun: Boolean): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra(EXTRA_FIRST_RUN, firstRun)
            return intent
        }

    }

    lateinit var fragments: Array<Fragment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_toolbar_back)

        firstRun = intent.getBooleanExtra(EXTRA_FIRST_RUN, false)

        if (firstRun) {
            val dialog = UiUtils.showAlertDialog(this, getString(R.string.no_settings_warning))
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "Custom"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            dialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "Use Default"
            ) { dialog, _ ->
                btSave.callOnClick()
                dialog.dismiss()
            }
            dialog.show()



            btCancel.visibility = View.GONE
        }

        val pair = setupApiRadioGroup()
        fragments = pair.first
        val currentSelection = pair.second

        viewPager.adapter = SettingsFragmentAdapter(
            fragments,
            supportFragmentManager
        )

        viewPager.currentItem = currentSelection


        radioGroupApis.setOnCheckedChangeListener { group, checkedId ->
            val index = radioGroupApis.indexOfChild(group?.findViewById<RadioButton>(checkedId))
            viewPager.setCurrentItem(index, true)
        }

        btSave.setOnClickListener {
            saveSettings()
        }
        btCancel.setOnClickListener {
            onBackPressed()
        }


    }

    private fun setupApiRadioGroup(): Pair<Array<Fragment>, Int> {
        var currentSelection = 0
        var params = RadioGroup.LayoutParams(
            0,
            RadioGroup.LayoutParams.WRAP_CONTENT,
            1f
        )
        val fragments = ArrayList<Fragment>()
        CameraApi.values().forEachIndexed { index, cameraApi ->
            val radioButton = RadioButton(ContextThemeWrapper(this, R.style.radionbutton), null, 0)
            radioButton.layoutParams = params
            radioButton.text = cameraApi.name
            radioButton.textSize = 12f
            radioButton.setPadding(UiUtils.dpToPx(10), 0, UiUtils.dpToPx(10), 0)
            radioButton.setTextColor(ContextCompat.getColor(this, R.color.colorTransparentWhite))
            if (cameraApi == Prefs.get(this, CameraApi::class.java)) {
                currentSelection = index
            }
            when (cameraApi) {
                CameraApi.OLD -> {
                    fragments.add(OldApiSettingsFragment.instance())
                }
                CameraApi.CAMERA2 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fragments.add(Camera2ApiSettingsFragment.instance())
                    } else {
                        radioButton.isEnabled = false
                    }
                }
                CameraApi.NDK -> {
                    fragments.add(OldApiSettingsFragment.instance())
                    radioButton.isEnabled = false
                }
            }
            radioGroupApis.addView(radioButton)
        }
        (radioGroupApis.getChildAt(currentSelection) as RadioButton).isChecked = true
        return Pair(fragments.toTypedArray(), currentSelection)
    }

    private fun saveSettings() {
        GlobalScope.launch(Dispatchers.Main) {
            viewProgress.visibility = View.VISIBLE
            delay(1000)
            val index = radioGroupApis.indexOfChild(
                radioGroupApis.findViewById<RadioButton>(
                    radioGroupApis.checkedRadioButtonId
                )
            )
            val cameraApi = when (index) {
                0 -> CameraApi.OLD
                1 -> CameraApi.CAMERA2
                2 -> CameraApi.NDK
                else -> null
            }
            val settings = (fragments[index] as BaseSettingsFragment).getSettings()
            Prefs.put(this@SettingsActivity, settings)
            Prefs.put(this@SettingsActivity, cameraApi!!)
            viewProgress.visibility = View.GONE
            setResult(Activity.RESULT_OK)
            finish()

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

}