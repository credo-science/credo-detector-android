package science.credo.mobiledetector.utils

import android.content.Context
import com.google.gson.Gson


object Prefs {

    class Keys{
        companion object{
            const val USER_LOGIN = "USER_LOGIN"
            const val USER_TOKEN = "USER_TOKEN"
            const val USER_PASSWORD = "USER_PASSWORD"
            const val CAMERA2_ENABLED = "CAMERA2_ENABLED"

        }
    }



    private const val PREFS_FILENAME = " cience.credo.mobiledetector"




    fun <T : Any> put(context: Context, jsonString: String?, cls: Class<T>, key: String) {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs.edit()
        editor.putString(cls.simpleName.toLowerCase() + key, jsonString)
        editor.apply()
    }

    fun <T : Any> put(context: Context, obj: T) {
        put(context, obj, "")
    }

    fun <T : Any> put(context: Context, obj: T, key: String) {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs.edit()
        editor.putString(
            obj::class.java.simpleName.toLowerCase() + key,
            Gson().toJson(obj))
        editor.apply()
    }

    fun <T> get(context: Context, cls: Class<T>): T? {
        return get(context, cls, "")
    }

    fun <T> get(context: Context, cls: Class<T>, key: String): T? {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
        val json = prefs.getString(cls.simpleName.toLowerCase() + key, null)
        return Gson().fromJson(json, cls)
    }


}