package com.example.bdui.ui.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bdui.ui.theme.AppColors

@Composable
fun CustomLoadingButton(
    textIdl: String,
    textErr: String = "Error",
    isLoading: Boolean = false,
    isError: Boolean = false,
    color: Color? = null,
    enabled: Boolean = true,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = color ?: AppColors.accentPrimary,
            disabledContainerColor = color ?: AppColors.surface,
        ),
        onClick = {
            onClick()
        },
        enabled = enabled,
        border = border,
        contentPadding = contentPadding,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(modifier)
            .height(48.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp),
                    color = AppColors.background
                )
            }
            isError -> {
                Text(
                    text = textErr,
                    color = AppColors.background
                )
            }
            else -> {
                Text(
                    text = textIdl,
                    color = AppColors.background
                )
            }
        }
    }
}