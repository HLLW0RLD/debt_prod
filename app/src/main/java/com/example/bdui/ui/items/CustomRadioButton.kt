package com.example.bdui.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.bdui.ui.theme.AppColors

@Composable
fun CustomRadioButton(
    selected: Boolean,
    text: String,
    color: Color? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .then(modifier)
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                onClick()
            },
    ) {
        RadioButton(
            selected = selected,
            onClick = { onClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = color ?: AppColors.accentPrimary,
                unselectedColor = AppColors.surface
            )
        )
        Text(
            text = text,
            color = if (selected) AppColors.textPrimary else AppColors.textSecondary
        )
    }
}