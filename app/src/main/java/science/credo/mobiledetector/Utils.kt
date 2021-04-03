package science.credo.mobiledetector

import android.content.Context

fun translateMessage(context: Context, message: CharSequence?, defaultValue: CharSequence = ""): CharSequence {
    if (message == null || message == "") {
        return defaultValue
    }

    if (message == "Login failed. Reason: Invalid username/email and password combination or unverified email.") {
        return context.getText(R.string.login_message_login_failed)
    }

    if (message == "Connection error ") {
        return context.getText(R.string.login_message_connection_failed)
    }

    return message;
}