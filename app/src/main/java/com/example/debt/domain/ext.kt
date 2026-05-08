package com.example.debt.domain

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

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
