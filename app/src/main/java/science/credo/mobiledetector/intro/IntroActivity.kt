package science.credo.mobiledetector.intro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import science.credo.mobiledetector.R
import science.credo.mobiledetector.utils.Prefs


class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        Prefs.put(this, "False", "showIntro")

        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        if (viewPager != null) {
            val adapter = IntroPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
        }
    }

}