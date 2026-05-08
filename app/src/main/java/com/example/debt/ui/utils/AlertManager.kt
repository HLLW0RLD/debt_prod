package com.example.debt.ui.utils

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.debt.R
import com.example.debt.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

enum class AlertType {
    ERROR,
    INFO
}

data class AlertMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val type: AlertType = AlertType.ERROR,
    val onActionClick: (() -> Unit)? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Int = 3000 // ms
)

class AlertManager {
    private val _messages = MutableStateFlow<List<AlertMessage>>(emptyList())
    val messages = _messages.asStateFlow()


    fun showError(text: String, onActionClick: (() -> Unit)? = null) {
        addMessage(AlertMessage(text = text, type = AlertType.ERROR, onActionClick = onActionClick))
    }

    fun showInfo(text: String, onActionClick: (() -> Unit)? = null) {
        addMessage(AlertMessage(text = text, type = AlertType.INFO, onActionClick = onActionClick))
    }



    private fun addMessage(message: AlertMessage) {
        _messages.value = buildList {
            addAll(_messages.value)
            add(message)
            if (size > 5) take(5) else this
        }
    }

    fun dismissMessage(id: String) {
        _messages.value = _messages.value.filter { it.id != id }
    }
}




/*--------------------------------VIEW------------------------------*/
@Composable
fun TopAlertHost(
    snackbarManager: AlertManager = koinInject(),
    modifier: Modifier = Modifier
) {
    val messages by snackbarManager.messages.collectAsState()

    Box(modifier = modifier.fillMaxWidth()) {
        messages.forEachIndexed { index, message ->
            key(message.id) {
                TopAlertBar(
                    index = index,
                    message = message.text,
                    type = message.type,
                    onActionClick = message.onActionClick?.let {
                        {
                            it()
                            snackbarManager.dismissMessage(message.id)
                        }
                    },
                    onDismiss = { snackbarManager.dismissMessage(message.id) },
                    durationMillis = message.duration,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
private fun TopAlertBar(
    index: Int,
    message: String,
    type: AlertType,
    onActionClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    durationMillis: Int = 3000,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val baseOffset = 24.dp
    val itemHeight = 64.dp
    val targetOffset = baseOffset + (itemHeight * index)

    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val swipeDistance = screenWidthPx * 1.2f

    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(
        0f to 0,
        -swipeDistance to 1,
        swipeDistance to 2
    )

    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "snackbar_alpha"
    )
    LaunchedEffect(Unit) {
        delay(index * 100L)
        isVisible = true
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(durationMillis.toLong())
            scope.launch {
                swipeableState.animateTo(1)
                delay(300L)
                onDismiss()
            }
        }
    }

    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue != 0) {
            delay(300L)
            onDismiss()
        }
    }

    val offset by animateDpAsState(
        targetValue = if (isVisible) targetOffset else (targetOffset - 80.dp),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "snackbar_offset"
    )

    val backgroundColor = when (type) {
        AlertType.ERROR -> AppColors.error
        AlertType.INFO -> AppColors.accentPrimary
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, with(density) { offset.roundToPx() }) }
            .padding(horizontal = 16.dp)
            .alpha(alpha)
            .zIndex(1000f - index.toFloat())
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                )
                .offset { IntOffset(swipeableState.offset.value.toInt(), 0) },
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (onActionClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            onActionClick()
                            scope.launch {
                                swipeableState.animateTo(1)
                                delay(300L)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trade),
                            contentDescription = "",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}