package com.example.bdui.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bdui.ui.theme.AppColors

@Composable
fun CustomPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Пароль",
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    showPasswordToggle: Boolean = true,
    modifier: Modifier = Modifier,
    focusedTextColor: Color = AppColors.textPrimary,
    unfocusedTextColor: Color = AppColors.textPrimary,
    focusedLabelColor: Color = AppColors.accentPrimary,
    unfocusedLabelColor: Color = AppColors.textPrimary,
    focusedBorderColor: Color = AppColors.accentPrimary,
    unfocusedBorderColor: Color = AppColors.textPrimary
) {
    var passwordVisible by remember { mutableStateOf(false) }

    CustomOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        focusedTextColor = focusedTextColor,
        unfocusedTextColor = unfocusedTextColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        modifier = modifier,
        trailingIcon = if (showPasswordToggle) {
            {
                Text(
                    text = if (passwordVisible) "Скрыть" else "Показать",
                    color = if (value.isEmpty()) AppColors.textSecondary else AppColors.accentPrimary,
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .width(64.dp)
                        .height(32.dp)
                )
            }
        } else null
    )
}
