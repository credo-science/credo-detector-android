package science.credo.mobiledetector2.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import science.credo.mobiledetector2.App
import science.credo.mobiledetector2.R
import science.credo.mobiledetector2.SplashActivity
import science.credo.mobiledetector2.detector.DetectorActivity
import science.credo.mobiledetector2.settings.SettingsActivity
import science.credo.mobiledetector2.utils.LocationHelper
import science.credo.mobiledetector2.utils.Prefs
import science.credo.mobiledetector2.statistics.StatisticsActivity
import science.credo.mobiledetector2.utils.UpdateTimeBroadcastReceiver


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
            R.id.menuStatistics -> {
                startActivity(
                    Intent(
                        this,
                        StatisticsActivity::class.java
                    )
                )
            }
            R.id.menuLogout -> {
                Prefs.put(this, null, String::class.java, Prefs.Keys.USER_LOGIN)
                Prefs.put(this, null, String::class.java, Prefs.Keys.USER_PASSWORD)
                startActivity(SplashActivity.intent(this))
                finish()
                drawerLayout.closeDrawer(Gravity.LEFT)
            }
            R.id.menuClassificationApp -> {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://user.credo.science/user-interface/classification/auth/?token=${App.token}")
                )
                startActivity(intent)
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
