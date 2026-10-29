package com.example.bdui.ui.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bdui.ui.theme.AppColors
import com.example.bdui_example.R

@Composable
fun CustomTimePickerField(
    label: String,
    time: String,
    isError: Boolean = false,
    onTimeChanged: (String) -> Unit,
    iconColor: Color = AppColors.accentPrimary,
    focusedPlaceholderColor: Color = AppColors.accentPrimary,
    unfocusedPlaceholderColor: Color = AppColors.textPrimary,
    focusedLabelColor: Color = AppColors.accentPrimary,
    unfocusedLabelColor: Color = AppColors.surface,
    focusedBorderColor: Color = AppColors.accentPrimary,
    unfocusedBorderColor: Color = AppColors.textPrimary,
    shape: Shape = RoundedCornerShape(25.dp),
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        CustomOutlinedTextField(
            value = time,
            onValueChange = { raw ->
                val digits = raw.filter(Char::isDigit).take(4)
                onTimeChanged(digits)
            },
            label = label,
            isError = isError,
            shape = shape,
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.clock),
                        contentDescription = "Выбрать время",
                        tint = iconColor
                    )
                }
            },
            visualTransformation = TimeVisualTransformation(),
            focusedLabelColor = focusedLabelColor,
            unfocusedLabelColor = unfocusedLabelColor,
            focusedBorderColor = focusedBorderColor,
            focusedPlaceholderColor = focusedPlaceholderColor,
            unfocusedPlaceholderColor = unfocusedPlaceholderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            keyboardType = KeyboardType.Number
        )
    }

    if (showTimePicker) {
        CustomTimePickerDialog(
            initialTime = time,
            color = iconColor,
            onTimeSelected = {
                onTimeChanged(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTimePickerDialog(
    initialTime: String,
    color: Color,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (hour, minute) = remember(initialTime) {
        val parts = initialTime.split(":")
        val parsedHour = parts.getOrNull(0)?.filter(Char::isDigit)?.toIntOrNull()
        val parsedMinute = parts.getOrNull(1)?.filter(Char::isDigit)?.toIntOrNull()
        (parsedHour?.takeIf { it in 0..23 } ?: 0) to
            (parsedMinute?.takeIf { it in 0..59 } ?: 0)
    }

    val state = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            CustomTextButton(text = "OK", color = color) {
                onTimeSelected("%02d:%02d".format(state.hour, state.minute))
            }
        },
        dismissButton = {
            CustomTextButton(text = "Отмена", color = color, onClick = onDismiss)
        },
        text = { TimePicker(state = state) },
        containerColor = AppColors.background
    )
}

private class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text.take(4)
        val transformed = when {
            source.length <= 2 -> source
            else -> source.substring(0, 2) + ":" + source.substring(2)
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 2) offset else (offset + 1).coerceAtMost(transformed.length)

            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 2) offset else (offset - 1).coerceAtMost(source.length)
        }
        return TransformedText(AnnotatedString(transformed), mapping)
    }
}
