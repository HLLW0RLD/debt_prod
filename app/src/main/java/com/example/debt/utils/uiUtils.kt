package com.example.debt.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.debt.App
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.ranges.contains

@RequiresApi(Build.VERSION_CODES.O)
fun String.getDateStatus(): DateStatus? {
    try {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        if (!this.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
            return null
        }

        val inputDate = LocalDate.parse(this, formatter)
        val currentDate = LocalDate.now()
        val daysDifference = ChronoUnit.DAYS.between(currentDate, inputDate)

        return when {
            daysDifference > 8 -> DateStatus.VERY_FAR
            daysDifference in 3..7 -> DateStatus.FAR
            daysDifference in 1..2 -> DateStatus.CLOSE
            daysDifference == 0L -> DateStatus.TODAY
            else -> DateStatus.PAST
        }
    } catch (e: Exception) {
        return null
    }
}

sealed class DateStatus {
    object VERY_FAR : DateStatus()
    object FAR : DateStatus()
    object CLOSE : DateStatus()
    object TODAY : DateStatus()
    object PAST : DateStatus()
}



fun Any.toast(msg: Any?, duration: Int = LENGTH_SHORT) {
    Toast.makeText(
        App.appInstance,
        if (msg == null) this.toString() else msg.toString(),
        duration)
        .show()
}



class DateVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val cleanDigits = text.text.filter { it.isDigit() }.take(8)

        val formatted = buildString {
            for (i in cleanDigits.indices) {
                append(cleanDigits[i])
                if ((i == 1 || i == 3) && i != cleanDigits.lastIndex) {
                    append(".")
                }
            }
        }

        val offsetTranslator = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> offset + 2
                }.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }.coerceAtLeast(0)
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetTranslator
        )
    }
}



inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    navController: NavController,
    crossinline content: @Composable (T) -> Unit
) {
    composable<T>(
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() }
    ) { backStackEntry ->

        BackHandler {
            navController.popBackStack()
        }
        val screen = backStackEntry.toRoute<T>()
        content(screen)
    }
}

fun enterTransition(): EnterTransition {
    return slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
}

fun exitTransition(): ExitTransition {
    return ExitTransition.None
}

fun popEnterTransition(): EnterTransition {
    return EnterTransition.None
}

fun popExitTransition(): ExitTransition {
    return slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
}


internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}



val LENGTH_LONG = Toast.LENGTH_LONG
val LENGTH_SHORT = Toast.LENGTH_SHORT

val LocalNavController =
    staticCompositionLocalOf<NavController> { throw IllegalStateException("No NavController found") }