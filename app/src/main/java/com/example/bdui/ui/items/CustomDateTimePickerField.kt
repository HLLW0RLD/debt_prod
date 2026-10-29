package com.example.bdui.ui.items

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bdui_example.R
import com.example.bdui.domain.utils.getCurrentDateTime
import com.example.bdui.ui.theme.AppColors
import com.example.bdui.domain.utils.toDateString
import com.example.bdui.domain.utils.toDateTimestamp
import kotlin.text.ifEmpty

@Composable
fun CustomDateTimePickerField(
    label: String,
    date: String,
    isError: Boolean = false,
    onDateChanged: (String) -> Unit,
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
    var showDatePicker by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = date)) }

    LaunchedEffect(date) {
        if (date != textFieldValue.text) {
            textFieldValue = TextFieldValue(text = date)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        CustomOutlinedTextField(
            value = date,
            onValueChange = { rawInput ->
                val digits = rawInput.filter { it.isDigit() }.take(8)
                val newValue = textFieldValue.copy(text = digits)
                textFieldValue = newValue
                onDateChanged(newValue.text)
            },
            isError = isError,
            label = label,
            shape = shape,
            trailingIcon = {
                IconButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.clock),
                        contentDescription = "",
                        tint = iconColor
                    )
                }
            },
            visualTransformation = DateVisualTransformation(),
            focusedLabelColor = focusedLabelColor,
            unfocusedLabelColor = unfocusedLabelColor,
            focusedBorderColor = focusedBorderColor,
            focusedPlaceholderColor = focusedPlaceholderColor,
            unfocusedPlaceholderColor = unfocusedPlaceholderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            keyboardType = KeyboardType.Number
        )
    }

    if (showDatePicker) {
        ReturnCustomPickerDialog(
            initialDate = date.ifEmpty { getCurrentDateTime(time = false) },
            onDateSelected = { newDate ->
                onDateChanged(newDate)
            },
            onDismiss = { showDatePicker = false },
            color = iconColor
        )
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReturnCustomPickerDialog(
    initialDate: String? = null,
    color: Color? = null,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(initialDate) {
        initialDate?.toDateTimestamp()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            CustomTextButton(color = color, text = "OK") {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selectedDate = millis.toDateString()
                    onDateSelected(selectedDate)
                }
                onDismiss()
            }
        },
        dismissButton = {
            CustomTextButton(text = "Отмена", color = color) {
                onDismiss()
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = color ?: AppColors.accentPrimary,
        ),
    ) {
        DatePicker(state = datePickerState)
    }
}

private class DateVisualTransformation : VisualTransformation {

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

