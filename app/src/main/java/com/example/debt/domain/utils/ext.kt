package com.example.debt.domain.utils

import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern


fun isValidEmail(email: String): Boolean {
    val expression = "^[a-zA-Z0-9+_.-]+@[a-z]+\\.+[a-z]+"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    return pattern.matcher(email).matches()
}

fun String.validateTelegram(): Boolean {
    val username = this
        .trim()
        .removePrefix("@")

    val regex = Regex("^(?!.*__)[a-zA-Z][a-zA-Z0-9_]{4,31}$")
    return regex.matches(username)
}


// parse date -------------------------------------------

fun String?.parseToFormat(
    fromPattern: String = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    toPattern: String = "dd.MM.yyyy"
): String {
    if (this == null) {
        return ""
    }
    return try {
        val date = SimpleDateFormat(fromPattern, Locale.getDefault()).parse(this)
        date?.let {
            SimpleDateFormat(toPattern, Locale.getDefault()).format(it)
        } ?: ""
    } catch (e: Exception) {
        ""
    }
}

fun String?.toDateTimestamp(pattern: String = "dd.MM.yyyy"): Long {
    if (this == null) return 0L
    return try {
        SimpleDateFormat(
            pattern,
            Locale.getDefault()
        ).parse(this)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

fun Long?.toDateString(pattern: String = "dd.MM.yyyy"): String {
    return SimpleDateFormat(pattern, Locale.getDefault())
        .format(Date(this ?: 0L))
}


// get date ---------------------------------------------

fun getCurrentDateTimeUTC(): String = reformatUTC("yyyy-MM-dd'T'HH:mm:ss'Z'")

fun getCurrentDateTime(
    date: Boolean = true,
    time: Boolean = true,
    year: Boolean = true,
    second: Boolean = true,
): String = when {
    date && time -> {
        reformatUTC("HH:mm:ss dd.MM.yyyy")
    }
    date && !time -> {
        if (year) {
            reformatUTC("dd.MM.yyyy")
        } else {
            reformatUTC("dd.MM")
        }
    }
    !date && time -> {
        if (second) {
            reformatUTC("HH:mm:ss")
        } else {
            reformatUTC("HH:mm")
        }
    }
    else -> reformatUTC("HH:mm:ss dd.MM.yyyy")
}

private fun reformatUTC(pattern: String): String {
    return DateTimeFormatter.ofPattern(pattern)
        .withZone(ZoneOffset.UTC)
        .format(Instant.now())
}




