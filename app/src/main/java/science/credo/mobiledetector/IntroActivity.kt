package science.credo.mobiledetector

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import science.credo.mobiledetector.fragment.appintro.IntroPagerAdapter


class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val pm = PreferenceManager.getDefaultSharedPreferences(this)
        val pmEditor = pm.edit()
        pmEditor.putBoolean("showIntro", false)
        pmEditor.apply()

        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        if (viewPager != null) {
            val adapter = IntroPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
        }
    }

}

