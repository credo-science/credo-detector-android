package science.credo.mobiledetector.info

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.jetbrains.anko.defaultSharedPreferences
import science.credo.mobiledetector.R
import java.security.MessageDigest

/**
 * Created by poznan on 24/08/2017.
 */
class UserInfo (context: Context){
    var username: String?
    var displayName: String? = null
    var email: String? = null
    var password: String? = null
    var team: String? = null
    var language: String? = null
    var key: String?= null

    init {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        username = pref.getString("user_name","")
        email    = pref.getString("user_email_address","")
        team     = pref.getString("user_team","")
        key      = pref.getString("user_key","")
    }

    data class UserData (val username: String, val email: String, val key: String, val team: String, val language: String){}
    data class UserDataRegister (val username: String, val displayName: String, val password: String, val email: String, val team: String, val language: String){}
    data class UserDataLogin (val email: String, val password: String){}
    data class UserDataInfo (val displayName: String, val team: String, val language: String){}

    fun getUserData(): UserData {
        return UserData (username!!, email!!, key!!, team!!, language!!)
    }
    fun getUserDataRegister(): UserDataRegister {
        return UserDataRegister(username!!, displayName!!, password!!, email!!, team!!, language!!)
    }
    fun getUserDataLogin(): UserDataLogin {
        return UserDataLogin (email!!, password!!)
    }
    fun getUserDataInfo(): UserDataInfo {
        return UserDataInfo (displayName!!, team!!, language!!)
    }
    companion object {
        private var mUserInfo: UserInfo? = null;
        fun getNewInstance(context: Context) = UserInfo(context)
    }
}