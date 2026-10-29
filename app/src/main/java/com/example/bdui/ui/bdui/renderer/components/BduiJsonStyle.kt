package com.example.bdui.ui.bdui.renderer.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.example.bdui.ui.theme.AppColors
import com.example.bdui_example.R
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Shared JSON readers and style conversion for all BDUI renderers. */
internal fun JsonObject.string(key: String, default: String = ""): String =
    this[key]?.let { (it as? JsonPrimitive)?.contentOrNull } ?: default

internal fun JsonObject.boolean(key: String, default: Boolean = false): Boolean =
    this[key]?.let { (it as? JsonPrimitive)?.contentOrNull?.toBooleanStrictOrNull() } ?: default

internal fun JsonObject.int(key: String, default: Int): Int =
    this[key]?.let { (it as? JsonPrimitive)?.contentOrNull?.toIntOrNull() } ?: default

internal fun JsonObject.float(key: String, default: Float): Float =
    this[key]?.let { (it as? JsonPrimitive)?.contentOrNull?.toFloatOrNull() } ?: default

@Composable
internal fun JsonObject.color(key: String, default: Color): Color =
    this[key]?.let { parseBduiColor((it as? JsonPrimitive)?.contentOrNull, default) } ?: default

@Composable
internal fun parseBduiColor(value: String?, default: Color): Color {
    if (value.isNullOrBlank()) return default
    val normalized = value.trim()
    return when (normalized.lowercase()) {
        "textprimary" -> AppColors.textPrimary
        "textsecondary" -> AppColors.textSecondary
        "accentprimary" -> AppColors.accentPrimary
        "accentsecondary" -> AppColors.accentSecondary
        "background" -> AppColors.background
        "backgroundsecondary" -> AppColors.backgroundSecondary
        "surface" -> AppColors.surface
        "surfacesecondary" -> AppColors.surfaceSecondary
        "error" -> AppColors.error
        "success" -> AppColors.success
        "onaccent" -> androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
        else -> try {
            val hex = normalized.removePrefix("#")
            when (hex.length) {
                6 -> Color(android.graphics.Color.parseColor("#$hex"))
                8 -> Color(android.graphics.Color.parseColor("#$hex"))
                else -> default
            }
        } catch (_: IllegalArgumentException) { default }
    }
}

internal fun JsonObject.paddingValues(key: String = "padding", default: Int = 0): PaddingValues {
    val value = this[key]
    val primitive = value as? JsonPrimitive
    val uniform = primitive?.contentOrNull?.toFloatOrNull()?.dp ?: default.dp
    val obj = value as? JsonObject
    if (obj == null) return PaddingValues(uniform)
    return PaddingValues(
        start = obj.float("start", default.toFloat()).dp,
        top = obj.float("top", default.toFloat()).dp,
        end = obj.float("end", default.toFloat()).dp,
        bottom = obj.float("bottom", default.toFloat()).dp
    )
}

internal fun JsonObject.shape(key: String = "cornerRadius", default: Float = 0f): Shape =
    RoundedCornerShape(float(key, default).dp)

internal fun JsonObject.layoutModifier(base: Modifier = Modifier): Modifier {
    var result = base
    val widthValue = this["width"]?.let { (it as? JsonPrimitive)?.contentOrNull }
    val heightValue = this["height"]?.let { (it as? JsonPrimitive)?.contentOrNull }
    result = result.then(sizeModifier(widthValue, heightValue))
    if (this["padding"] != null) result = result.padding(paddingValues())
    val bg = this["backgroundColor"]
    if (bg != null) {
        // Color conversion is composable, so background is applied by BduiStyledBox where needed.
    }
    return result
}

internal fun sizeModifier(width: String?, height: String?): Modifier {
    var result: Modifier = Modifier

    result = when (width?.lowercase()) {
        "match_parent" -> result.fillMaxWidth()
        "match_parent_height" -> result.fillMaxHeight()
        "match_parent_size" -> result.fillMaxSize()
        "wrap_content", null, "" -> result
        else -> width.toFloatOrNull()?.let { result.width(it.dp) } ?: result
    }

    result = when (height?.lowercase()) {
        "match_parent" -> result.fillMaxHeight()
        "wrap_content", null, "" -> result
        else -> height.toFloatOrNull()?.let { result.height(it.dp) } ?: result
    }

    return result
}

internal fun fontWeight(value: String): FontWeight = when (value.lowercase()) {
    "medium" -> FontWeight.Medium
    "semibold", "semi_bold" -> FontWeight.SemiBold
    "bold" -> FontWeight.Bold
    "light" -> FontWeight.Light
    else -> FontWeight.Normal
}

internal fun textAlign(value: String): TextAlign = when (value.lowercase()) {
    "center" -> TextAlign.Center
    "end", "right" -> TextAlign.End
    "justify" -> TextAlign.Justify
    else -> TextAlign.Start
}

internal fun textOverflow(value: String): TextOverflow = when (value.lowercase()) {
    "ellipsis" -> TextOverflow.Ellipsis
    "visible" -> TextOverflow.Visible
    else -> TextOverflow.Clip
}

internal fun textDecoration(value: String): TextDecoration = when (value.lowercase()) {
    "underline" -> TextDecoration.Underline
    "linethrough", "line_through" -> TextDecoration.LineThrough
    else -> TextDecoration.None
}

internal fun fontStyle(value: String): FontStyle = if (value.equals("italic", true)) FontStyle.Italic else FontStyle.Normal


@Composable
internal fun BduiPainter(name: String, fallbackId: Int = R.drawable.baseline_add_circle_24): Painter {
    val context = LocalContext.current
    val resourceName = name.substringAfterLast('/').substringBeforeLast('.').lowercase()
    val id = rememberDrawableId(context, resourceName)
    return painterResource(if (id != 0) id else fallbackId)
}

@Composable
internal fun BduiIcon(
    name: String,
    tint: Color,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceName = name.substringAfterLast('/').substringBeforeLast('.').lowercase()
    val id = rememberDrawableId(context, resourceName)
    if (id != 0) {
        Icon(painter = painterResource(id), contentDescription = contentDescription, tint = tint, modifier = modifier)
    } else {
        val glyph = when (resourceName) {
            "arrow_back", "arrow_left", "back" -> "‹"
            "close", "clear" -> "×"
            "search" -> "⌕"
            "more_vert", "more", "options" -> "⋮"
            "add", "plus" -> "+"
            "check" -> "✓"
            "edit" -> "✎"
            "delete", "trash" -> "⌫"
            "home" -> "⌂"
            "settings" -> "⚙"
            else -> "•"
        }
        Text(glyph, color = tint, fontSize = 22.sp, modifier = modifier)
    }
}

private fun rememberDrawableId(context: android.content.Context, name: String): Int =
    context.resources.getIdentifier(name, "drawable", context.packageName)
