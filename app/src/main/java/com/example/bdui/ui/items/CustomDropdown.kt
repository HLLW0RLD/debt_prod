@file:JvmName("CustomDropdownKt")

package com.example.bdui.ui.items

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.bdui.ui.theme.AppColors
import com.example.bdui_example.R

@Composable
fun <T> CustomDropdown(
    currentFilter: T,
    onFilterSelected: (T) -> Unit,
    filterOptions: List<T>,
    getFilterText: (T) -> String,
    isSelectedEqual: (T, T) -> Boolean = { a, b -> a == b },
    containerColor: Color = AppColors.surface,
    textColor: Color = AppColors.textPrimary,
    selectedTextColor: Color = AppColors.accentPrimary,
    iconTint: Color = AppColors.accentPrimary,
    selectedIcon: Painter? = null,
    dropdownIcon: Painter = painterResource(R.drawable.ic_double_arrow_down),
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(containerColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isExpanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getFilterText(currentFilter),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                painter = dropdownIcon,
                contentDescription = "",
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (isExpanded) 180f else 0f),
                tint = iconTint
            )
        }

        if (isExpanded) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { isExpanded = false }
            ) {
                Card(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .shadow(8.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor)
                ) {
                    Column {
                        filterOptions.forEach { filter ->
                            val isSelected = isSelectedEqual(filter, currentFilter)

                            Row(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onFilterSelected(filter)
                                        isExpanded = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = getFilterText(filter),
                                    color = if (isSelected) selectedTextColor else textColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                                if (isSelected && selectedIcon != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = selectedIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = selectedTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}