package science.credo.mobiledetector

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import science.credo.mobiledetector.database.DataManager
import science.credo.mobiledetector.database.DetectionStateWrapper
import science.credo.mobiledetector.database.UserInfoWrapper
import science.credo.mobiledetector.fragment.*
import science.credo.mobiledetector.fragment.detections.DetectionContent
import science.credo.mobiledetector.info.ConfigurationInfo
import science.credo.mobiledetector.info.PowerConnectionReceiver
import android.os.PowerManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import com.instacart.library.truetime.TrueTimeRx
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.database.ConfigurationWrapper
import science.credo.mobiledetector.network.ServerInterface
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import kotlinx.android.synthetic.main.fragment_status.*
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.toast
import science.credo.mobiledetector.synchronization.UpdateTimeBroadcastReceiver
import java.util.*
import java.util.concurrent.TimeUnit

const val REQUEST_SIGNUP = 1

class MainActivity : AppCompatActivity(),
        StatusFragment.OnFragmentInteractionListener,
        CredoFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        DetectionFragment.OnListFragmentInteractionListener {

    val mUpdateTimeBroadcastReceiver = UpdateTimeBroadcastReceiver()

    override fun onStartDetection() {
        ConfigurationInfo(this).isDetectionOn = true
        credoApplication().turnOnDetection()
        DetectionStateWrapper.getLatestSession(this).clear()
        DetectionStateWrapper.getLatestSession(this).startDetectionTimestamp = TrueTimeRx.now().time
    }

    override fun onStopDetection() {
        ConfigurationInfo(this).isDetectionOn = false
        credoApplication().turnOffDetection()
    }

    override fun onListFragmentInteraction(item: DetectionContent.HitItem?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

//    var mHitDataManager : HitDataManager? = null

    var mSettingsFlag = false;
    val mReceiver: PowerConnectionReceiver = PowerConnectionReceiver()


    companion object {
        val TAG = "MainActivity"
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        if (!ConfigurationWrapper(this).autoRun) {
            onStopDetection()
        }
        super.onDestroy()
    }

    override fun onFragmentInteraction(uri: Uri) {
        Log.d(TAG, "onFragmentInteraction: " + uri.toString())
        when (uri.fragment) {
            "StatusFragment" -> {
                when (uri.path) {
                    "start" -> {
                        onStartDetection()
                    }
                    "stop" -> {
                        onStopDetection()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        initRxTrueTime()
        initRxTrueTimeScheduler()

        if (UserInfoWrapper(this).token.isEmpty()) {
            setResult(Activity.RESULT_OK)
            finish()
            return
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {
            Log.d(TAG, "onNavigationItemSelected")
            val id = it.itemId
            switchFragment(id)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        /*navigationView.view().imageView.onClick {
            val href = "https://api.credo.science/"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(href))
            startActivity(browserIntent)
        }*/

        if (credoApplication().detectorRunning.get()) {
            switchFragment(R.id.nav_status)
        } else {
            switchFragment(R.id.nav_information)
        }

        toggle.syncState()
    }

    private fun initRxTrueTimeScheduler(){
        mUpdateTimeBroadcastReceiver.setAlarm(this)

    }

    private fun initRxTrueTime() {

//        ToDo 1. Check if internet if not get prompt to turn on.
//        ToDo 2. Start synchronize service.
//        ToDo 2. Check inside if internet is on if not get Flag NO_SYNCH and use local time instead. repeat
//        ToDo 2. Check if alarm is ON already

        val disposable = TrueTimeRx.build()
                .withConnectionTimeout(31428)
                .withRetryCount(100)
                .withSharedPreferencesCache(this)
                .initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ date ->
                    Log.d(TAG, "initRxTrueTime - Success initialized TrueTime :$date")
                }, {
                    Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", it)
                })
    }

    override fun updateTrueTime() {
        initRxTrueTime()
        mUpdateTimeBroadcastReceiver.logTrueTime("updateTrueTime")
        mUpdateTimeBroadcastReceiver.toastTrueTime(this)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed")
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")

        return when (item.itemId) {
            R.id.action_settings -> {
                startSettingsActivity()
                true
            }
            R.id.action_logout -> {
                //startRegisterActivity()
                UserInfoWrapper(this).token = ""
                UserInfoWrapper(this).email = ""
                UserInfoWrapper(this).password = ""
                UserInfoWrapper(this).login = ""
                UserInfoWrapper(this).displayName = ""
                UserInfoWrapper(this).team = ""
                setResult(Activity.RESULT_OK)
                ConfigurationWrapper(this).endpoint = ConfigurationWrapper.defaultEndpoint
                finish()
                true
            }
            R.id.action_account -> {
                startActivity(Intent(this@MainActivity, UserActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGNUP) {
            if (UserInfoWrapper(this).token.isEmpty()) {
                this.finish()
            }
        }
    }

    fun switchFragment(id: Int): Boolean {

        var fragment: Fragment? = null
        var title: String? = null;
        var lead = "${getString(R.string.app_name_short)}:"
        when (id) {
            R.id.nav_status -> {
                title = "$lead ${getString(R.string.title_status)}"
                fragment = StatusFragment.newInstance()
            }

            /* R.id.nav_debug -> {
                 title = "$lead ${getString(R.string.title_statistics)}"
                 fragment = DebugFragment.newInstance(getString(R.string.title_statistics), "")
             }*/
            R.id.nav_settings -> {
                startSettingsActivity()
                mSettingsFlag = true
            }
            R.id.nav_register -> {
                //startRegisterActivity()
                mSettingsFlag = true
            }
            R.id.nav_statistics -> {
                title = getString(R.string.preview_title_activity, DataManager.TRIM_PERIOD_HITS_DAYS)
                fragment = DetectionFragment.newInstance(1)
            }
            R.id.nav_information -> {
                title = "$lead ${getString(R.string.title_information)}"
                fragment = CredoFragment.newInstance()

            }
        }

        if (fragment != null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.content_frame, fragment)
            ft.commit()
        }

        // set the toolbar title
        if (supportActionBar != null && title != null) supportActionBar!!.title = title

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        mSettingsFlag = false;
        disableDoze()

        doAsync {
            val db = DataManager.getDefault(this@MainActivity)
            val si = ServerInterface.getDefault(this@MainActivity)

            try {
                // TODO: detect internet connection and try flush when is
                db.trimHitsDb()
                db.sendHitsToNetwork(si)
                db.flushCachedPings(si)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to sent to server", e)
            }
            db.closeDb()
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
//        restartService()
        super.onPause()
    }

    fun disableDoze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun startSettingsActivity() = startActivity(Intent(this, SettingsActivity::class.java))

    override fun onGoToExperiment() {
        switchFragment(R.id.nav_status)
    }

    private fun credoApplication(): CredoApplication = application as CredoApplication
}
