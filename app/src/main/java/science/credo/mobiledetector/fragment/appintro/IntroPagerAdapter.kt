package science.credo.mobiledetector.fragment.appintro

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class IntroPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

    private val COUNT_FRAGMENT = 3

    override fun getItem(position: Int): Fragment? {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = Intro1Fragment()
            1 -> fragment = Intro2Fragment()
            2 -> fragment = Intro3Fragment()
        }
        return fragment
    }

    override fun getCount(): Int {
        return COUNT_FRAGMENT
    }


}
