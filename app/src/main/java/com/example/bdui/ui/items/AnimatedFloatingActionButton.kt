package com.example.bdui.ui.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.bdui.ui.theme.AppColors

@Composable
fun AnimatedFloatingActionButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter,
    containerColor: Color = AppColors.accentPrimary,
    contentColor: Color = AppColors.background,
    enterDelay: Int = 0,
    startOffsetDp: Float = 80f,
    size: Float = 56f,
    iconSize: Float = 24f,
    cornerRadius: Float = 28f,
    width: Float? = null,
    height: Float? = null
) {
    val density = LocalDensity.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = {
                with(density) { startOffsetDp.dp.roundToPx() }
            },
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = enterDelay,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = enterDelay,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = 100, delayMillis = enterDelay)
        ),
        exit = slideOutVertically(
            targetOffsetY = {
                with(density) { startOffsetDp.dp.roundToPx() }
            },
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(cornerRadius.dp),
            modifier = Modifier
                .padding(16.dp)
                .then(if (width != null) Modifier.width(width.dp) else Modifier)
                .then(if (height != null) Modifier.height(height.dp) else if (width == null) Modifier.size(size.dp) else Modifier.height(size.dp)),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}