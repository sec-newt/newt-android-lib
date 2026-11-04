package io.github.secnewt.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** What a color means: go/good, danger, information, or nothing in particular. */
enum class NewtTone { Good, Danger, Info, Neutral }

/** The tone's color, at 7:1 or better against the background and lifted surfaces. */
@Composable
@ReadOnlyComposable
fun NewtTone.color(): Color = toneColor(this, MaterialTheme.colorScheme, MaterialTheme.colorScheme.isDark())

/** The blue-to-purple fill for "on" and chosen things (the same colors as [neonOutline]). */
@Composable
@ReadOnlyComposable
fun newtGradient(): Brush =
    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))

/** Text and icons on [newtGradient]: near-black in dark mode, white in light mode. */
@Composable
@ReadOnlyComposable
fun onNewtGradient(): Color = onGradient(MaterialTheme.colorScheme.isDark())

internal fun toneColor(tone: NewtTone, scheme: ColorScheme, dark: Boolean): Color = when (tone) {
    NewtTone.Good -> if (dark) Color(0xFF9ECE6A) else Color(0xFF2D5A0A)
    NewtTone.Danger -> scheme.error
    NewtTone.Info -> scheme.primary
    NewtTone.Neutral -> scheme.onSurfaceVariant
}

internal fun onGradient(dark: Boolean): Color = if (dark) Color(0xFF0A0B10) else Color.White

/** WCAG contrast ratio between two colors (1 to 21). */
internal fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

private fun ColorScheme.isDark(): Boolean = background.luminance() < 0.5f
