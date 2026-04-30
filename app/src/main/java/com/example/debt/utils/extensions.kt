package com.example.debt.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.debt.App
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.debt.R
import com.example.debt.app.utils.LogUtils.errorLog
import com.example.debt.data.model.Transaction
import com.example.debt.data.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

val LENGTH_LONG = Toast.LENGTH_LONG
val LENGTH_SHORT = Toast.LENGTH_SHORT

val LocalNavController =
    staticCompositionLocalOf<NavController> { throw IllegalStateException("No NavController found") }


fun Any.toast(msg: Any?, duration: Int = LENGTH_SHORT) {
    Toast.makeText(
        App.appInstance,
        if (msg == null) this.toString() else msg.toString(),
        duration)
        .show()
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDateTime(utc: Boolean = false): String {
    return if (utc) {
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(java.time.ZoneOffset.UTC)
            .format(java.time.Instant.now())
    } else {
        LocalDateTime
        .now(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"))
    }
}

fun TransactionType.getTransactionName(context: Context): String{
    return when (this) {
        TransactionType.DEBT -> context.getString(R.string.transaction_debt)
        TransactionType.PAYMENT -> context.getString(R.string.transaction_payment)
    }
}

fun String?.validateTelegram(): Boolean {
    val username = this ?: ""
        .trim()
        .removePrefix("@")

    val regex = Regex("^(?!.*__)[a-zA-Z][a-zA-Z0-9_]{4,31}$")

    return regex.matches(username)
}

fun isValidEmail(email: String): Boolean {
    var isValid = false
    val expression = "^[a-zA-Z0-9+_.-]+@[a-z]+\\.+[a-z]+"
    val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher: Matcher = pattern.matcher(email)
    if (matcher.matches()) {
        isValid = true
    }
    return isValid
}

fun String.openTelegramChat() {
    val context = App.appInstance
    val nick = this.replace("@", "")
    val telegramUrl = "https://t.me/$nick"
    val intent = Intent(Intent.ACTION_VIEW, telegramUrl.toUri())

    try {
        context.packageManager.getPackageInfo("org.telegram.messenger", 0)
        intent.setPackage("org.telegram.messenger")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

    } catch (e: Exception) {
        toast("Telegram не установлен")
        errorLog(e)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.setColorDate(): Color? {
    try {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        if (!this.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
            return null
            throw IllegalArgumentException("Дата должна быть в формате dd.MM.yyyy")
        }

        val inputDate = LocalDate.parse(this, formatter)
        val currentDate = LocalDate.now()

        val daysDifference = ChronoUnit.DAYS.between(currentDate, inputDate)

        return when {
            daysDifference > 8 -> Color.Green
            daysDifference in 3..7 -> Color.Yellow
            daysDifference in 1..2 -> Color.hsl(35f, 1f, 0.5f)
            daysDifference == 0L -> Color.Red
            else -> Color.White
        }
    } catch (e : Exception) {
        errorLog("string: $this \nerror $e")
    }
    return Color.Red
}

internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}