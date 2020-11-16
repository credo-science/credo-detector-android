package science.credo.mobiledetector2.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_intro.*
import science.credo.mobiledetector2.R
import science.credo.mobiledetector2.login.LoginActivity
import science.credo.mobiledetector2.utils.Prefs


class IntroActivity : AppCompatActivity() {

    private val viewPagerOnChangeCallback= object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(poisition: Int) {
            introScreenTitleTextView.setText(titleResources[poisition])
            introScreenImageView.setImageResource(imageResources[poisition])
            introScreenDescriptionTextView.setText(descriptionResources[poisition])
            finishIntroButton.setText(buttonResources[poisition])
        }
    }

    private companion object {
        private val titleResources = mutableListOf(
            R.string.fragment_intro_text_top_1,
            R.string.fragment_intro_text_top_2,
            R.string.fragment_intro_text_top_3,
            R.string.fragment_intro_text_top_4,
            R.string.fragment_intro_text_top_5
        )

        private val imageResources = mutableListOf(
            R.mipmap.logotypcredo,
            R.mipmap.slider_2,
            R.mipmap.slider_3,
            R.mipmap.slider_4,
            R.mipmap.slider_5
        )

        private val descriptionResources = mutableListOf(
            R.string.fragment_intro_text_bottom_1,
            R.string.fragment_intro_text_bottom_2,
            R.string.fragment_intro_text_bottom_3,
            R.string.fragment_intro_text_bottom_4,
            R.string.fragment_intro_text_bottom_5
        )

        private val buttonResources = mutableListOf(
            R.string.intro_skip,
            R.string.intro_skip,
            R.string.intro_skip,
            R.string.intro_skip,
            R.string.intro_start
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        finishIntroButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        Prefs.put(this, "False", "showIntro")

        IntroPagerAdapter().apply {
            submitList(titleResources)
            submitList(imageResources)
            submitList(descriptionResources)
            submitList(buttonResources)
        }.also { introScreenViewPager.adapter = it }

        wormIndicator.setViewPager2(introScreenViewPager)

        introScreenViewPager.registerOnPageChangeCallback(viewPagerOnChangeCallback)
    }
}