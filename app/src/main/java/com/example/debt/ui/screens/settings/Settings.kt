package com.example.debt.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debt.R
import com.example.debt.ui.items.CustomRadioButton
import com.example.debt.ui.theme.AppColors
import com.example.debt.ui.theme.ThemeMode
import com.example.debt.ui.theme.AppThemeViewModel
import com.example.debt.ui.utils.LocalNavController
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object Settings

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    appThemeViewModel: AppThemeViewModel = koinViewModel()
) {

    val navController = LocalNavController.current

    val selectedTheme by appThemeViewModel.currentTheme.collectAsState()
    val isDarkTheme by appThemeViewModel.isDarkTheme.collectAsState()
    val isLightTheme by appThemeViewModel.isLightTheme.collectAsState()
    val isColoredTheme by appThemeViewModel.isColoredTheme.collectAsState()
    val isSystemTheme by appThemeViewModel.isSystemTheme.collectAsState()

    val context = LocalContext.current
    val versionName = remember {
        try {
            val packageName = context.packageManager.getPackageInfo(context.packageName, 0)
            packageName.versionName
        } catch (e: Exception) {
            "1.0"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(AppColors.background)
        ) {
            Spacer(Modifier.size(50.dp))
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(AppColors.background)
                    .fillMaxWidth(),
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        tint = AppColors.textPrimary,
                        contentDescription = "",
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary,
                )
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.theme_selection),
                    color = AppColors.textPrimary,
                    modifier = Modifier.padding(8.dp)
                )

                ThemeOption(
                    text = stringResource(R.string.theme_system),
                    selected = isSystemTheme,
                    onClick = {
                        appThemeViewModel.setTheme(ThemeMode.SYSTEM)
                    }
                )

                ThemeOption(
                    text = stringResource(R.string.theme_light),
                    selected = isLightTheme,
                    onClick = {
                        appThemeViewModel.setTheme(ThemeMode.LIGHT)
                    }
                )

                ThemeOption(
                    text = stringResource(R.string.theme_dark),
                    selected = isDarkTheme,
                    onClick = {
                        appThemeViewModel.setTheme(ThemeMode.DARK)
                    }
                )

                ThemeOption(
                    text = stringResource(R.string.theme_colored),
                    selected = isColoredTheme,
                    onClick = {
                        appThemeViewModel.setTheme(ThemeMode.COLOR)
                    }
                )
            }

            Spacer(Modifier.size(16.dp))
        }

        val verText = stringResource(R.string.app_version)
        Text(
            text = verText + " $versionName",
            fontSize = 14.sp,
            color = AppColors.textSecondary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = null
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomRadioButton(
            selected = selected,
            text = text,
            onClick = onClick,
        )
    }
}