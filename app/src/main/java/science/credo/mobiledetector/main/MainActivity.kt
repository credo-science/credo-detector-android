package science.credo.mobiledetector.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import science.credo.mobiledetector.R
import science.credo.mobiledetector.SplashActivity
import science.credo.mobiledetector.detector.DetectorActivity
import science.credo.mobiledetector.settings.SettingsActivity
import science.credo.mobiledetector.utils.LocationHelper
import science.credo.mobiledetector.utils.Prefs
import science.credo.mobiledetector.utils.SynchronizedTimeUtils
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import science.credo.mobiledetector.utils.UpdateTimeBroadcastReceiver

class MainActivity : AppCompatActivity(), DrawerAdapter.OnItemClick {

    val mUpdateTimeBroadcastReceiver = UpdateTimeBroadcastReceiver()

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startNtpSynchronization()

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_drawer)
//        cameraInterface = OldCameraInterface()
//        cameraInterface?.start(this)

        setupDrawer()

        btRunDetector.setOnClickListener {
            startActivity(DetectorActivity.intent(this))
        }
    }

    fun startNtpSynchronization() {
        mUpdateTimeBroadcastReceiver.initRxTrueTime(this)
        mUpdateTimeBroadcastReceiver.setAlarm(this)
    }

    fun setupDrawer() {
        val drawerMenu = MenuBuilder(this)
        menuInflater.inflate(R.menu.drawer, drawerMenu)
        rvDrawerMenu.layoutManager = LinearLayoutManager(this)
        rvDrawerMenu.adapter = DrawerAdapter(
            this,
            drawerMenu,
            this,
            "https://i.pinimg.com/474x/4f/c5/22/4fc52216fe539362b65aef4ba5a0cb52.jpg",
            "James"
        )

    }

    override fun onDrawerItemClick(menuItem: MenuItem) {
        onOptionsItemSelected(menuItem)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuStartDetector -> {
                btRunDetector.callOnClick()
                drawerLayout.closeDrawer(Gravity.LEFT)
            }
            R.id.menuSettings -> {
                println("============location ${LocationHelper.location}")
                startActivity(SettingsActivity.intent(this))
                drawerLayout.closeDrawer(Gravity.LEFT)
            }
            R.id.menuLogout -> {
                Prefs.put(this, null, String::class.java, Prefs.Keys.USER_LOGIN)
                Prefs.put(this, null, String::class.java, Prefs.Keys.USER_PASSWORD)
                startActivity(SplashActivity.intent(this))
                finish()
                drawerLayout.closeDrawer(Gravity.LEFT)
            }
            else -> {
                drawerLayout.openDrawer(Gravity.LEFT)
            }
        }

//        drawerLayout.openDrawer(Gravity.START)

        return true


    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
