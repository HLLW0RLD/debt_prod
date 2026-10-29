package com.example.bdui.ui.items

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bdui.ui.bdui.parser.BduiConfigException
import com.example.bdui.ui.theme.AppColors
import com.example.bdui_example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAppBar(
    title: String = stringResource(R.string.app_name),
    type: String = "default",
    showBack: Boolean = false,
    showOptions: Boolean = false,
    isCentered: Boolean = true,
    showDivider: Boolean = true,
    height: Float = 56f,
    textSize: Float = 24f,
    query: String = "",
    placeholder: String = "search",
    onQueryChange: ((String) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onOptionsClick: (() -> Unit)? = null,
    backIcon: Painter = painterResource(R.drawable.arrow_left),
    optionsIcon: Painter = painterResource(R.drawable.switcher_options),
    titleColor: Color = AppColors.textPrimary,
    backIconTint: Color = AppColors.textPrimary,
    optionsIconTint: Color = AppColors.textPrimary,
    backgroundColor: Color = AppColors.textPrimary,
    modifier: Modifier = Modifier
) {
    val normalizedType = type.trim().ifEmpty { "default" }

    Surface(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(height.dp),
        color = backgroundColor
    ) {
        Box {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (showBack) {
                    IconButton(
                        onClick = { onBackClick?.invoke() }
                    ) {
                        Icon(
                            painter = backIcon,
                            contentDescription = "",
                            tint = backIconTint
                        )
                    }
                } else if (isCentered) {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                when (normalizedType) {
                    "default" -> {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = titleColor,
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (!isCentered) {
                                        Modifier.padding(start = 8.dp)
                                    } else {
                                        Modifier
                                    }
                                ),
                            textAlign = if (isCentered) {
                                TextAlign.Center
                            } else {
                                TextAlign.Start
                            },
                            fontSize = textSize.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    "search" -> {
                        BasicTextField(
                            value = query,
                            onValueChange = { onQueryChange?.invoke(it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = titleColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = placeholder,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                                .copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    else -> {
                        throw BduiConfigException(
                            "Unsupported appBar.type='$normalizedType'"
                        )
                    }
                }

                if (normalizedType == "default" && showOptions) {
                    IconButton(onClick = { onOptionsClick?.invoke() }) {
                        Icon(
                            painter = optionsIcon,
                            contentDescription = "Дополнительные действия",
                            tint = optionsIconTint
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
            }

            if (showDivider) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    thickness = 0.5.dp,
                    color = AppColors.textPrimary
                )
            }
        }
    }
}