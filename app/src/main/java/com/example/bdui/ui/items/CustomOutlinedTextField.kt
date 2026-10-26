package com.example.bdui.ui.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bdui.ui.theme.AppColors

@Composable
fun CustomOutlinedTextField(
    value: String,
    label: String,
    cursorColor: Color = AppColors.accentPrimary,
    focusedTextColor: Color = AppColors.textPrimary,
    unfocusedTextColor: Color = AppColors.textPrimary,
    focusedPlaceholderColor: Color = AppColors.accentPrimary,
    unfocusedPlaceholderColor: Color = AppColors.textPrimary,
    focusedLabelColor: Color = AppColors.accentPrimary,
    unfocusedLabelColor: Color = AppColors.surfaceSecondary,
    errorLabelColor: Color = AppColors.error,
    focusedBorderColor: Color = AppColors.accentPrimary,
    unfocusedBorderColor: Color = AppColors.textPrimary,
    focusedContainerColor: Color = AppColors.background,
    unfocusedContainerColor: Color = AppColors.background,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    maxCharacters: Int? = null,
    showCharacterCounter: Boolean = false,
    onMaxCharactersExceeded: ((String) -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = RoundedCornerShape(25.dp),
    modifier: Modifier = Modifier,
) {
    fun processValueChange(newValue: String): String {
        return if (maxCharacters != null && newValue.length > maxCharacters) {
            val trimmedValue = newValue.take(maxCharacters)
            onMaxCharactersExceeded?.invoke(trimmedValue)
            trimmedValue
        } else {
            newValue
        }
    }

    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            shape = shape,
            value = value,
            onValueChange = { newValue ->
                onValueChange(processValueChange(newValue))
            },
            label = {
                Text(
                    text = label,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            enabled = enabled,
            readOnly = readOnly,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = cursorColor,
                focusedPlaceholderColor = focusedPlaceholderColor,
                unfocusedPlaceholderColor = unfocusedPlaceholderColor,
                focusedTextColor = focusedTextColor,
                unfocusedTextColor = unfocusedTextColor,
                focusedContainerColor = focusedContainerColor,
                unfocusedContainerColor = unfocusedContainerColor,
                focusedLabelColor = focusedLabelColor,
                unfocusedLabelColor = unfocusedLabelColor,
                errorLabelColor = errorLabelColor,
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor,
            ),
        )

        if (showCharacterCounter && maxCharacters != null && isFocused) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CharacterCounter(
                    currentLength = value.length,
                    maxLength = maxCharacters,
                    isError = value.length > maxCharacters
                )
            }
        }
    }
}

@Composable
fun CharacterCounter(
    currentLength: Int,
    maxLength: Int,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "$currentLength / $maxLength",
            fontSize = 12.sp,
            color = when {
                isError -> AppColors.error
                currentLength > maxLength * 0.9f -> AppColors.accentPrimary.copy(alpha = 0.7f)
                else -> AppColors.textPrimary.copy(alpha = 0.5f)
            }
        )
    }
}
