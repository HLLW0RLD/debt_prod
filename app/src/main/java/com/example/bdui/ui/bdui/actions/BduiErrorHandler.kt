package com.example.bdui.ui.bdui.actions

import com.example.bdui.ui.utils.AlertManager

class BduiErrorHandler(
    private val alertManager: AlertManager
) {

    fun handle(error: Exception) {
        val message = error.message
            ?.takeIf { it.isNotBlank() }
            ?: "An unexpected BDUI error occurred"

        alertManager.showError(message)
    }
}