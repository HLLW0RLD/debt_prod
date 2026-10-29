package com.example.bdui.ui.utils

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
import com.example.bdui.App
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.ranges.contains

fun Any.toast(msg: Any?, duration: Int = LENGTH_SHORT) {
    Toast.makeText(
        App.appInstance,
        if (msg == null) this.toString() else msg.toString(),
        duration)
        .show()
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