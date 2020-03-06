package science.credo.mobiledetector.settings

import androidx.fragment.app.Fragment

abstract class BaseSettingsFragment : Fragment(){

    abstract suspend fun getSettings() : BaseSettings

}