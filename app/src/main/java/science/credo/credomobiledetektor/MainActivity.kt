package science.credo.credomobiledetektor

import android.app.Activity
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
import science.credo.credomobiledetektor.database.DataManager
import science.credo.credomobiledetektor.database.UserInfoWrapper
import science.credo.credomobiledetektor.fragment.*
import science.credo.credomobiledetektor.fragment.detections.DetectionContent
import science.credo.credomobiledetektor.info.ConfigurationInfo
import science.credo.credomobiledetektor.info.PowerConnectionReceiver

const val REQUEST_SIGNUP = 1

class MainActivity : AppCompatActivity(),
        StatusFragment.OnFragmentInteractionListener,
        CredoFragment.OnFragmentInteractionListener,
        DebugFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        DetectionFragment.OnListFragmentInteractionListener {

    override fun onStartDetection() {
        ConfigurationInfo(this).isDetectionOn = true
        credoApplication().turnOnDetection()
    }

    override fun onStopDetection() {
        credoApplication().turnOffDetection()
        ConfigurationInfo(this).isDetectionOn = false
    }

    override fun onListFragmentInteraction(item: DetectionContent.HitItem?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

//    var mHitDataManager : HitDataManager? = null

    var mSettingsFlag = false;
    val mReceiver: PowerConnectionReceiver = PowerConnectionReceiver()

    companion object {
        val TAG = "MainActivity"
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        try {
            DataManager.getInstance(applicationContext).closeDb()
        } catch (e:Exception) {
            Log.w(TAG, e)
        }

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
        Log.d(TAG,"onCreate")

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
            Log.d(TAG,"onNavigationItemSelected")
            val id = it.itemId
            switchFragment(id)
            drawer.closeDrawer(GravityCompat.START)
             true
        }

        if (credoApplication().detectorRunning.get()) {
            switchFragment(R.id.nav_status)
        } else {
            switchFragment(R.id.nav_information)
        }

        toggle.syncState()
    }

    override fun onBackPressed() {
        Log.d(TAG,"onBackPressed")
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG,"onCreateOptionsMenu")
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG,"onOptionsItemSelected")

        return when (item.itemId) {
            R.id.action_settings -> {
                startSettingsActivity()
                true
            }
            R.id.action_logout -> {
                //startRegisterActivity()
                UserInfoWrapper(this).token = ""
                setResult(Activity.RESULT_OK)
                finish()
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
                title = "Detections (last ${DataManager.TRIMPERIOD_HITS_DAYS} days)"
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
        Log.d(TAG,"onResume")
        super.onResume()
        mSettingsFlag = false;
    }

    override fun onPause() {
        Log.d(TAG,"onPause")
//        restartService()
        super.onPause()
    }

    private fun startSettingsActivity() = startActivity(Intent(this, SettingsActivity::class.java))

    override fun onGoToExperiment() {
        switchFragment(R.id.nav_status)
    }

    private fun credoApplication() : CredoApplication = application as CredoApplication
}



