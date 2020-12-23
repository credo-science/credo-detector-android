package science.credo.mobiledetector2.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Looper
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import science.credo.mobiledetector2.R
import java.lang.IllegalStateException
import java.util.*


object UiUtils {

    private var screenWidth: Int = -1
    private var screenHeight: Int = -1


    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun timestampToReadableHour(ts : Long) :String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = ts
        return  String.format("%02d:%02d:%02d",calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND))
    }


    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (activity.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken, 0
            )
        }
    }

    fun showAlertDialog(context: Context, msg: String): AlertDialog {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }

        val alertDialog = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.app_name))
            .setMessage(msg)
            .setCancelable(true)
            .create()
        GlobalScope.launch(Dispatchers.Main) {
            alertDialog.show()
        }
        return alertDialog
    }


    fun createStyledRatioButton(context: Context): RadioButton {
        val rb = RadioButton(ContextThemeWrapper(context, R.style.radionbutton), null, 0)
        rb.textSize = 14f
        rb.setPadding(dpToPx(10), 0, dpToPx(10), 0)
        val params = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dpToPx(5)
        rb.layoutParams = params
        rb.setTextColor(ContextCompat.getColor(context, R.color.colorTransparentWhite))
        return rb
    }


    fun initScreenDimensions(activity: Activity) {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        if (screenHeight == -1) {
            throw  IllegalStateException("the screen size has not been initialized")
        }
        return screenHeight
    }

    fun getScreenWidth(): Int {
        if (screenWidth == -1) {
            throw  IllegalStateException("the screen size has not been initialized")
        }
        return screenWidth

    }
}

