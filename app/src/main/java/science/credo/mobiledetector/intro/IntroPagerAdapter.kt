package science.credo.mobiledetector.intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class IntroPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

    private val COUNT_FRAGMENT = 4

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment = Intro1Fragment()
        when (position) {
            0 -> fragment = Intro1Fragment()
            1 -> fragment = Intro2Fragment()
            2 -> fragment = Intro3Fragment()
            3 -> fragment = Intro4Fragment()
        }
        return fragment
    }

    override fun getCount(): Int {
        return COUNT_FRAGMENT
    }


}