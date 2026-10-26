package com.example.bdui.ui.bdui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bdui.ui.bdui.model.ScreenConfig
import com.example.bdui.ui.bdui.model.ScreenPage
import com.example.bdui.ui.bdui.parser.BduiConfigLoader
import com.example.bdui.ui.bdui.renderer.BduiRenderContext
import com.example.bdui.ui.bdui.renderer.BduiRenderer
import com.example.bdui.ui.theme.AppColors

@Composable
fun BduiScreenHost(
    page: ScreenPage,
    modifier: Modifier = Modifier,
    content: @Composable (ScreenConfig, ScreenPage) -> Unit
) {
    val context = LocalContext.current

    // produceState позволяет представить загрузку как состояние UI.
    // При смене screenId загружается другая конфигурация.
    val loadResult by produceState<Result<ScreenConfig>?>(
        initialValue = null,
        key1 = page.screenId
    ) {
        value = runCatching {
            // Loader пока читает локальные assets.
            // Позже источник можно заменить на API и кэш.
            BduiConfigLoader(context).load(page.screenId)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Пока результат не получен, показываем индикатор загрузки.
            loadResult == null -> {
                CircularProgressIndicator()
            }

            // При ошибке показываем понятное сообщение вместо падения UI.
            loadResult?.isFailure == true -> {
                val error = loadResult?.exceptionOrNull()

                Text(
                    text = "Не удалось загрузить экран " +
                            "'${page.screenId}': ${error?.message.orEmpty()}"
                )
            }

            // Конфигурация успешно загружена и проверена.
            else -> {
                val config = loadResult!!.getOrThrow()

                // Хост не знает, какие компоненты лежат в body.
                // Он передаёт конфигурацию следующему уровню.
                content(config, page)
            }
        }
    }
}