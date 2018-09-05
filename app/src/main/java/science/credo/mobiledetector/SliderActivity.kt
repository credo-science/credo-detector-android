package science.credo.mobiledetector

import android.os.Bundle
import android.support.v4.app.Fragment
import android.graphics.Color
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.AppIntroFragment.newInstance
import science.credo.mobiledetector.R.id.image


class SliderActivity : AppIntro() {
    private val titles by lazy { resources.getStringArray(R.array.slider_title) }
    private val descriptions by lazy { resources.getStringArray(R.array.slider_description) }
    private val colors = arrayOf(Color.parseColor("#333333"))
    // private val permissions = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(titles[0], descriptions[0], R.drawable.slider1, colors[0]))
        addSlide(AppIntroFragment.newInstance(titles[1], descriptions[1], R.drawable.slider2, colors[0]))
        addSlide(AppIntroFragment.newInstance(titles[2], descriptions[2], R.drawable.slider3, colors[0]))
        addSlide(AppIntroFragment.newInstance(titles[3], descriptions[3], R.drawable.slider4, colors[0]))

        showStatusBar(false)

        showSkipButton(true)
        this.progressButtonEnabled = true

        // Jesli chcemy od razu pytac o uprawnienia
        // askForPermissions(permissions, 0)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }

}