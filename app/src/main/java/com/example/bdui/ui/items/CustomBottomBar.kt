package com.example.bdui.ui.items

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bdui.ui.bdui.renderer.components.BduiIcon
import com.example.bdui.ui.theme.AppColors

/** Reusable bottom navigation component configurable from BDUI JSON. */
data class CustomBottomBarItem(
    val id: String,
    val label: String,
    val icon: String
)

@Composable
fun CustomBottomBar(
    items: List<CustomBottomBarItem>,
    selectedItem: String?,
    onItemClick: (CustomBottomBarItem) -> Unit,
    backgroundColor: Color = AppColors.surface,
    selectedColor: Color = AppColors.accentPrimary,
    unselectedColor: Color = AppColors.textSecondary,
    labelFontSize: Float = 12f,
    iconSize: Float = 24f,
    height: Float? = null
) {
    NavigationBar(
        modifier = if (height != null && height > 0f) Modifier.height(height.dp) else Modifier,
        containerColor = backgroundColor
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.id == selectedItem,
                onClick = { onItemClick(item) },
                icon = {
                    if (item.icon.isNotBlank()) {
                        BduiIcon(item.icon, if (item.id == selectedItem) selectedColor else unselectedColor, modifier = Modifier.size(iconSize.dp))
                    }
                },
                label = { Text(text = item.label, fontSize = labelFontSize.sp) },
                enabled = item.id.isNotBlank(),
                colors = NavigationBarItemColors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    selectedIndicatorColor = selectedColor.copy(alpha = 0.12f),
                    disabledIconColor = unselectedColor,
                    disabledTextColor = unselectedColor
                )
            )
        }
    }
}
