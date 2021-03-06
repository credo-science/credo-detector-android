package science.credo.mobiledetector2.settings

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import science.credo.mobiledetector2.R
import science.credo.mobiledetector2.analytics.SettingsChangeEvent
import science.credo.mobiledetector2.utils.Prefs
import science.credo.mobiledetector2.utils.UiUtils


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
            Prefs.put(this, true, Prefs.Keys.CAMERA2_ENABLED)
            if (index == 1 && Prefs.get(
                    this,
                    Boolean::class.java,
                    Prefs.Keys.CAMERA2_ENABLED
                ) != true
            ) {
                (radioGroupApis.getChildAt(0) as RadioButton).isChecked = true
                showInputDialog()
            } else {
                viewPager.setCurrentItem(index, true)
            }
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
//                    fragments.add(OldApiSettingsFragment.instance())
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
            SettingsChangeEvent(this@SettingsActivity, settings, cameraApi!!).send()
            Prefs.put(this@SettingsActivity, settings)
            Prefs.put(this@SettingsActivity, cameraApi)
            viewProgress.visibility = View.GONE
            setResult(Activity.RESULT_OK)
            finish()

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }


    fun showInputDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name))
        builder.setMessage(getString(R.string.trusted_mode_info))

        var password = "";
        val input = EditText(this)
        input.imeOptions = EditorInfo.IME_ACTION_DONE;
        input.isSingleLine = true
        input.gravity = Gravity.CENTER
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { dialog, _ ->
            password = input.text.toString()
            if (password == "credo2020") {
                Prefs.put(this, true, Prefs.Keys.CAMERA2_ENABLED)
                (radioGroupApis.getChildAt(1) as RadioButton).isChecked = true
            } else {
                UiUtils.showAlertDialog(this, getString(R.string.incorrect))
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        val dialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show()
    }
}