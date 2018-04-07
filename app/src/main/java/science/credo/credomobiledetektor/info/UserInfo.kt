package science.credo.credomobiledetektor.info

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.jetbrains.anko.defaultSharedPreferences
import science.credo.credomobiledetektor.R
import java.security.MessageDigest

/**
 * Created by poznan on 24/08/2017.
 */
class UserInfo (context: Context){
    var name: String?
    var email: String? = null
    var key: String?= null
    var team: String? = null

    init {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        name = pref.getString("user_name","")
        email = pref.getString("user_email_address","")

        team = pref.getString("user_team","")
        key = pref.getString("user_key","")
    }

    data class UserData (val name: String, val email: String, val key: String, val team: String){}
    data class UserDataRegister (val name: String, val email: String, val team: String){}

    fun getUserData(): UserData {
        return UserData (name!!, email!!, key!!, team!!)
    }
    fun getUserDataRegister(): UserDataRegister {
        return UserDataRegister(name!!, email!!, team!!)
    }
    companion object {
        private var mUserInfo: UserInfo? = null;
        fun getNewInstance(context: Context) = UserInfo(context)
    }
}