package com.example.bdui.ui.bdui.renderer.components

import android.app.TimePickerDialog
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.bdui.ui.bdui.renderer.screen.BduiScreenContext
import com.example.bdui.ui.items.AnimatedFloatingActionButton
import com.example.bdui.ui.items.CustomDateTimePickerField
import com.example.bdui.ui.items.CustomDialog
import com.example.bdui.ui.items.CustomDropdown
import com.example.bdui.ui.items.CustomOutlinedTextField
import com.example.bdui.ui.items.CustomRadioButton
import com.example.bdui.ui.theme.AppColors
import com.example.bdui_example.R
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Calendar

object BduiElementRegistry {
    val renderers: Map<String, BduiComponentRenderer> = mapOf(
        "text_view_default" to { node, context, modifier ->
            val text = context.resolve(node.string("text"))
            val color = node.color("color", AppColors.textPrimary)
            val bg = node["backgroundColor"]?.let { parseBduiColor((it as? JsonPrimitive)?.contentOrNull, Color.Transparent) } ?: Color.Transparent
            val corner = node.float("cornerRadius", 0f)
            val widthModifier = sizeModifier(node.string("width"), node.string("height"))
            val styled = modifier.then(widthModifier)
                .clip(RoundedCornerShape(corner.dp)).background(bg)
                .padding(node.paddingValues())
                .alpha(if (node.boolean("enabled", true)) 1f else 0.55f)
            Text(
                text = text,
                modifier = styled,
                color = color,
                fontSize = node.float("fontSize", 16f).sp,
                fontWeight = fontWeight(node.string("fontWeight", "normal")),
                fontStyle = fontStyle(node.string("fontStyle", "normal")),
                textAlign = textAlign(node.string("textAlign", "start")),
                lineHeight = node["lineHeight"]?.let { node.float("lineHeight", 0f).takeIf { v -> v > 0 }?.sp } ?: TextStyle.Default.lineHeight,
                letterSpacing = node.float("letterSpacing", 0f).sp,
                maxLines = node.int("maxLines", Int.MAX_VALUE).coerceAtLeast(1),
                minLines = node.int("minLines", 1).coerceAtLeast(1),
                overflow = textOverflow(node.string("overflow", "clip")),
                textDecoration = textDecoration(node.string("textDecoration", "none"))
            )
        },
        // Backward-compatible aliases for the first version of the BDUI config.
        "text" to { node, context, modifier ->
            Text(context.resolve(node.string("text")), modifier = modifier.fillMaxWidth())
        },
        "button_text_default" to { node, context, modifier ->
            val enabled = node.boolean("enabled", true)
            Button(
                onClick = { runAction(node, context) },
                enabled = enabled,
                modifier = modifier.then(sizeModifier(node.string("width", "match_parent"), node.string("height", "48"))),
                shape = RoundedCornerShape(node.float("cornerRadius", 12f).dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = node.color("containerColor", AppColors.accentPrimary),
                    contentColor = node.color("contentColor", androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                ),
                contentPadding = node.paddingValues("contentPadding", 16)
            ) { Text(context.resolve(node.string("text")), fontSize = node.float("fontSize", 16f).sp, fontWeight = fontWeight(node.string("fontWeight", "medium"))) }
        },
        "button_icon_default" to { node, context, modifier ->
            val size = node.float("size", 48f).dp
            Button(
                onClick = { runAction(node, context) }, enabled = node.boolean("enabled", true),
                modifier = modifier.size(size), shape = RoundedCornerShape(node.float("cornerRadius", 12f).dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = node.color("containerColor", AppColors.accentPrimary),
                    contentColor = node.color("contentColor", androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                ), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) { BduiIcon(node.string("icon"), node.color("contentColor", androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)) }
        },
        "button_text_icon_default" to { node, context, modifier ->
            Button(
                onClick = { runAction(node, context) }, enabled = node.boolean("enabled", true), modifier = modifier,
                shape = RoundedCornerShape(node.float("cornerRadius", 12f).dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = node.color("containerColor", AppColors.accentPrimary),
                    contentColor = node.color("contentColor", androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                ), contentPadding = node.paddingValues("contentPadding", 16)
            ) {
                val iconFirst = node.string("iconPosition", "start") != "end"
                if (iconFirst) BduiIcon(node.string("icon"), node.color("contentColor", androidx.compose.material3.MaterialTheme.colorScheme.onPrimary), modifier = Modifier.padding(end = 8.dp))
                Text(context.resolve(node.string("text")))
                if (!iconFirst) BduiIcon(node.string("icon"), node.color("contentColor", androidx.compose.material3.MaterialTheme.colorScheme.onPrimary), modifier = Modifier.padding(start = 8.dp))
            }
        },
        "button_text_ghost" to { node, context, modifier ->
            TextButton(onClick = { runAction(node, context) }, enabled = node.boolean("enabled", true), modifier = modifier) {
                Text(context.resolve(node.string("text")), color = node.color("contentColor", AppColors.accentPrimary), fontSize = node.float("fontSize", 16f).sp)
            }
        },
        "button_icon_ghost" to { node, context, modifier ->
            IconButton(onClick = { runAction(node, context) }, enabled = node.boolean("enabled", true), modifier = modifier.then(node["size"]?.let { Modifier.size(node.float("size", 48f).dp) } ?: Modifier)) {
                BduiIcon(node.string("icon"), node.color("contentColor", AppColors.accentPrimary))
            }
        },
        "button_floating_action" to { node, context, modifier ->
            AnimatedFloatingActionButton(
                visible = node.boolean("visible", true),
                onClick = { runAction(node, context) },
                modifier = modifier,
                icon = BduiPainter(node.string("icon", "baseline_add_circle_24")),
                containerColor = node.color("containerColor", AppColors.accentPrimary),
                contentColor = node.color("contentColor", AppColors.background),
                enterDelay = node.int("enterDelay", 0),
                startOffsetDp = node.float("startOffsetDp", 80f),
                size = node.float("size", 56f),
                iconSize = node.float("iconSize", 24f),
                cornerRadius = node.float("cornerRadius", 28f),
                width = node["width"]?.jsonPrimitive?.contentOrNull?.toFloatOrNull(),
                height = node["height"]?.jsonPrimitive?.contentOrNull?.toFloatOrNull()
            )
        },
        "field_input_outlined" to { node, context, modifier ->
            renderOutlinedField(node, context, modifier, password = false)
        },
        "textField" to { node, context, modifier ->
            renderOutlinedField(node, context, modifier, password = false)
        },
        "field_input_password" to { node, context, modifier ->
            renderOutlinedField(node, context, modifier, password = true)
        },
        "field_input_date" to { node, context, modifier ->
            val key = node.string("stateKey")
            LaunchedEffect(key) { if (key !in context.values) context.values[key] = node.string("date") }
            val value = context.values[key].orEmpty()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CustomDateTimePickerField(
                    label = context.resolve(node.string("label", "Дата")), date = value,
                    isError = node.boolean("isError", false), onDateChanged = { context.values[key] = it },
                    iconColor = node.color("iconColor", AppColors.accentPrimary),
                    focusedPlaceholderColor = node.color("focusedPlaceholderColor", AppColors.accentPrimary),
                    unfocusedPlaceholderColor = node.color("unfocusedPlaceholderColor", AppColors.textPrimary),
                    focusedLabelColor = node.color("focusedLabelColor", AppColors.accentPrimary),
                    unfocusedLabelColor = node.color("unfocusedLabelColor", AppColors.surface),
                    focusedBorderColor = node.color("focusedBorderColor", AppColors.accentPrimary),
                    unfocusedBorderColor = node.color("unfocusedBorderColor", AppColors.textPrimary),
                    shape = node.shape("cornerRadius", 25f), modifier = modifier.then(sizeModifier(node.string("width"), node.string("height")))
                )
            } else {
                CustomOutlinedTextField(value = value, label = context.resolve(node.string("label", "Дата")), onValueChange = { context.values[key] = it }, modifier = modifier)
            }
        },
        "field_input_time" to { node, context, modifier ->
            val contextAndroid = LocalContext.current
            val key = node.string("stateKey")
            LaunchedEffect(key) { if (key !in context.values) context.values[key] = node.string("value") }
            val value = context.values[key].orEmpty()
            Box(modifier = modifier.then(sizeModifier(node.string("width", "match_parent"), node.string("height")))) {
                CustomOutlinedTextField(
                    value = value, label = context.resolve(node.string("label", "Время")), readOnly = true,
                    enabled = node.boolean("enabled", true), isError = node.boolean("isError", false),
                    onValueChange = { context.values[key] = it }, modifier = Modifier.fillMaxWidth()
                )
                Box(Modifier.matchParentSize().clickable(enabled = node.boolean("enabled", true)) {
                    val parts = value.split(":")
                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val minute = parts.getOrNull(1)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.MINUTE)
                    TimePickerDialog(contextAndroid, { _, h, m -> context.values[key] = "%02d:%02d".format(h, m) }, hour, minute, true).show()
                })
            }
        },
        "field_input_dropdown" to { node, context, modifier ->
            val key = node.string("stateKey")
            LaunchedEffect(key) { if (key !in context.values) context.values[key] = node.string("currentFilter") }
            val options = node["options"] as? JsonArray ?: JsonArray(emptyList())
            val selected = context.values[key].orEmpty()
            val selectedLabel = options.firstOrNull { option -> (option as? JsonObject)?.string("value") == selected }
                ?.let { (it as? JsonObject)?.string("label") }.orEmpty()
            CustomDropdown(
                currentFilter = selectedLabel.ifEmpty { selected },
                onFilterSelected = { label ->
                    val option = options.firstOrNull { (it as? JsonObject)?.string("label") == label }
                    context.values[key] = (option as? JsonObject)?.string("value") ?: label
                },
                filterOptions = options.map { (it as? JsonObject)?.string("label").orEmpty() },
                getFilterText = { it },
                containerColor = node.color("containerColor", AppColors.surface),
                textColor = node.color("textColor", AppColors.textPrimary),
                selectedTextColor = node.color("selectedTextColor", AppColors.accentPrimary),
                iconTint = node.color("iconTint", AppColors.accentPrimary),
                selectedIcon = node["selectedIcon"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let { BduiPainter(it) },
                dropdownIcon = BduiPainter(node.string("dropdownIcon", "ic_double_arrow_down"), R.drawable.ic_double_arrow_down),
                modifier = modifier.then(sizeModifier(node.string("width"), node.string("height")))
            )
        },
        "selection_radio_default" to { node, context, modifier ->
            val key = node.string("stateKey")
            val value = node.string("value")
            LaunchedEffect(key, value) { if (key !in context.values && node.boolean("selected", false)) context.values[key] = value }
            CustomRadioButton(
                selected = context.values[key] == value,
                text = context.resolve(node.string("text")),
                color = node["color"]?.let { parseBduiColor((it as? JsonPrimitive)?.contentOrNull, AppColors.accentPrimary) },
                onClick = { context.values[key] = value }, modifier = modifier
            )
        },
        "selection_switch_default" to { node, context, modifier ->
            val key = node.string("stateKey")
            val checked = context.values[key]?.toBooleanStrictOrNull() ?: node.boolean("checked", false)
            Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                if (node.string("text").isNotBlank()) Text(context.resolve(node.string("text")), modifier = Modifier.weight(1f))
                Switch(checked = checked, enabled = node.boolean("enabled", true), onCheckedChange = { context.values[key] = it.toString() }, colors = SwitchDefaults.colors(checkedThumbColor = node.color("checkedColor", AppColors.accentPrimary), uncheckedThumbColor = node.color("uncheckedColor", AppColors.surfaceSecondary)))
            }
        },
        "selection_checkbox_default" to { node, context, modifier ->
            val key = node.string("stateKey")
            val checked = context.values[key]?.toBooleanStrictOrNull() ?: node.boolean("checked", false)
            Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, enabled = node.boolean("enabled", true), onCheckedChange = { context.values[key] = it.toString() }, colors = CheckboxDefaults.colors(checkedColor = node.color("checkedColor", AppColors.accentPrimary), uncheckedColor = node.color("uncheckedColor", AppColors.surfaceSecondary)))
                if (node.string("text").isNotBlank()) Text(context.resolve(node.string("text")))
            }
        },
        "dialog_view_default" to { node, context, _ ->
            var visible by remember(node) { mutableStateOf(node.boolean("visible", true)) }
            if (visible) {
                AlertDialog(
                    onDismissRequest = { visible = false; runNamedAction(node, "dismissAction", context) },
                    title = node.string("title").takeIf { it.isNotBlank() }?.let { { Text(context.resolve(it)) } },
                    text = node.string("message").takeIf { it.isNotBlank() }?.let { { Text(context.resolve(it)) } },
                    confirmButton = { TextButton(onClick = { visible = false; runNamedAction(node, "confirmAction", context) }) { Text(node.string("confirmTitle", "OK")) } },
                    dismissButton = { TextButton(onClick = { visible = false; runNamedAction(node, "cancelAction", context) }) { Text(node.string("dismissTitle", "Отмена")) } }
                )
            }
        },
        // Backward-compatible button config.
        "button" to { node, context, modifier ->
            Button(onClick = { runAction(node, context) }, modifier = modifier.fillMaxWidth()) { Text(context.resolve(node.string("text"))) }
        }
    )

    @Composable
    private fun renderOutlinedField(node: JsonObject, context: BduiScreenContext, modifier: Modifier, password: Boolean) {
        val key = node.string("stateKey")
        if (key.isBlank()) {
            Text("Ошибка BDUI: у поля отсутствует stateKey", modifier = modifier.fillMaxWidth())
            return
        }
        LaunchedEffect(key) { if (key !in context.values) context.values[key] = node.string("value") }
        val value = context.values[key].orEmpty()
        val keyboardType = when (node.string("keyboardType", if (password) "Password" else "Text").lowercase()) {
            "number", "numeric" -> KeyboardType.Number
            "decimal" -> KeyboardType.Decimal
            "phone" -> KeyboardType.Phone
            "email", "emailaddress" -> KeyboardType.Email
            "password" -> KeyboardType.Password
            "numberpassword" -> KeyboardType.NumberPassword
            "uri" -> KeyboardType.Uri
            else -> KeyboardType.Text
        }
        var passwordVisible by remember(node) { mutableStateOf(node.boolean("passwordVisible", false)) }
        val transformation = when {
            password && !passwordVisible && node.boolean("showPasswordToggle", true) -> PasswordVisualTransformation()
            node.string("visualTransformation", if (password) "Password" else "None").equals("password", true) && !passwordVisible -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        }
        Column(modifier = modifier.then(sizeModifier(node.string("width"), node.string("height")))) {
            CustomOutlinedTextField(
                value = value,
                label = context.resolve(node.string("label")),
                cursorColor = node.color("cursorColor", AppColors.accentPrimary),
                focusedTextColor = node.color("focusedTextColor", AppColors.textPrimary),
                unfocusedTextColor = node.color("unfocusedTextColor", AppColors.textPrimary),
                focusedPlaceholderColor = node.color("focusedPlaceholderColor", AppColors.accentPrimary),
                unfocusedPlaceholderColor = node.color("unfocusedPlaceholderColor", AppColors.textPrimary),
                focusedLabelColor = node.color("focusedLabelColor", AppColors.accentPrimary),
                unfocusedLabelColor = node.color("unfocusedLabelColor", AppColors.surfaceSecondary),
                errorLabelColor = node.color("errorLabelColor", AppColors.error),
                focusedBorderColor = node.color("focusedBorderColor", AppColors.accentPrimary),
                unfocusedBorderColor = node.color("unfocusedBorderColor", AppColors.textPrimary),
                focusedContainerColor = node.color("focusedContainerColor", AppColors.background),
                unfocusedContainerColor = node.color("unfocusedContainerColor", AppColors.background),
                enabled = node.boolean("enabled", true), readOnly = node.boolean("readOnly", false),
                isError = node.boolean("isError", false), onValueChange = { context.values[key] = it },
                trailingIcon = if (password && node.boolean("showPasswordToggle", true)) {
                    { IconButton(onClick = { passwordVisible = !passwordVisible }) { BduiIcon(if (passwordVisible) "visibility_off" else "visibility", AppColors.accentPrimary) } }
                } else null,
                singleLine = node.boolean("singleLine", true), maxLines = if (node.boolean("singleLine", true)) 1 else node.int("maxLines", Int.MAX_VALUE),
                minLines = node.int("minLines", 1), maxCharacters = node["maxCharacters"]?.jsonPrimitive?.contentOrNull?.toIntOrNull(),
                showCharacterCounter = node.boolean("showCharacterCounter", false), keyboardType = keyboardType,
                visualTransformation = transformation, shape = node.shape("cornerRadius", 25f),
                modifier = Modifier.fillMaxWidth()
            )
            if (node.boolean("isError", false) && node.string("errorText").isNotBlank()) {
                Text(context.resolve(node.string("errorText")), color = AppColors.error, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp))
            }
        }
    }

    private fun runAction(node: JsonObject, context: BduiScreenContext) {
        val action = node["action"] as? JsonObject ?: return
        context.actionHandler.executeNavigationAction(action)
    }

    private fun runNamedAction(node: JsonObject, key: String, context: BduiScreenContext) {
        val action = node[key] as? JsonObject ?: return
        context.actionHandler.executeNavigationAction(action)
    }
}
