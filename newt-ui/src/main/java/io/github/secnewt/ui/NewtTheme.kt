package io.github.secnewt.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.secnewt.ui.R

/**
 * Fonts, all bundled (no downloads), shared by every Newt app:
 * Chakra Petch for screen titles only, Atkinson Hyperlegible (designed for low vision)
 * for everything you read, and JetBrains Mono for numbers so digits line up.
 */
object NewtFonts {
    val Display = FontFamily(
        Font(R.font.chakra_semibold, FontWeight.SemiBold),
        Font(R.font.chakra_bold, FontWeight.Bold),
    )
    val Body = FontFamily(
        Font(R.font.atkinson_regular, FontWeight.Normal),
        Font(R.font.atkinson_bold, FontWeight.Bold),
    )
    val Mono = FontFamily(
        Font(
            R.font.jetbrains_mono,
            FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            R.font.jetbrains_mono,
            FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        ),
    )
}

// Tokyo Night on true black. Text and accents stay at or above 7:1 against the background;
// neon is for outlines and highlights, never behind text you have to read. See NEWT_STYLE.md.
/** The dark color scheme, for apps that build on it (like Newt Home's terminal colors). */
val NewtNight = darkColorScheme(
    primary = Color(0xFF85ABF8),
    onPrimary = Color(0xFF0B0C14),
    primaryContainer = Color(0xFF253055),
    onPrimaryContainer = Color(0xFFDCE3FF),
    secondary = Color(0xFFBB9AF7),
    onSecondary = Color(0xFF14091F),
    secondaryContainer = Color(0xFF2E2552),
    onSecondaryContainer = Color(0xFFEDE4FF),
    tertiary = Color(0xFF7DCFFF),
    onTertiary = Color(0xFF041520),
    background = Color(0xFF050608),
    onBackground = Color(0xFFE2E6FF),
    surface = Color(0xFF050608),
    onSurface = Color(0xFFE2E6FF),
    surfaceVariant = Color(0xFF161925),
    onSurfaceVariant = Color(0xFFB4BBDD),
    surfaceContainerLowest = Color(0xFF050608),
    surfaceContainerLow = Color(0xFF0E1018),
    surfaceContainer = Color(0xFF12141E),
    surfaceContainerHigh = Color(0xFF161925),
    surfaceContainerHighest = Color(0xFF1C2030),
    outline = Color(0xFF565F89),
    outlineVariant = Color(0xFF2A2F45),
    error = Color(0xFFFF7A93),
    onError = Color(0xFF1F0610),
)

// Tokyo Night Day, for when the phone is in light mode.
/** The light color scheme. */
val NewtDay = lightColorScheme(
    primary = Color(0xFF1A43A3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE3FF),
    onPrimaryContainer = Color(0xFF0B1B4A),
    secondary = Color(0xFF5E33B0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6DCFF),
    onSecondaryContainer = Color(0xFF1E0A45),
    tertiary = Color(0xFF00597A),
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF1A1B26),
    surface = Color.White,
    onSurface = Color(0xFF1A1B26),
    surfaceVariant = Color(0xFFECEEF6),
    onSurfaceVariant = Color(0xFF2F3549),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F7FB),
    surfaceContainer = Color(0xFFF0F2F8),
    surfaceContainerHigh = Color(0xFFECEEF6),
    surfaceContainerHighest = Color(0xFFE4E7F1),
    outline = Color(0xFF6B7194),
    outlineVariant = Color(0xFFCDD1E3),
    error = Color(0xFF94102A),
    onError = Color.White,
)

private val Base = Typography()

/**
 * Newt text styles: Chakra Petch for big titles, Atkinson Hyperlegible for reading. No reading
 * style is under 16sp (names and row titles 19–21sp); the phone's own font size applies on top.
 */
val NewtTypography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = NewtFonts.Display),
    displayMedium = Base.displayMedium.copy(fontFamily = NewtFonts.Display),
    displaySmall = Base.displaySmall.copy(fontFamily = NewtFonts.Display),
    headlineLarge = Base.headlineLarge.copy(fontFamily = NewtFonts.Display),
    headlineMedium = Base.headlineMedium.copy(fontFamily = NewtFonts.Display),
    headlineSmall = Base.headlineSmall.copy(fontFamily = NewtFonts.Display),
    titleLarge = Base.titleLarge.reading(21),
    titleMedium = Base.titleMedium.reading(19),
    titleSmall = Base.titleSmall.reading(17),
    bodyLarge = Base.bodyLarge.reading(17),
    bodyMedium = Base.bodyMedium.reading(16),
    bodySmall = Base.bodySmall.reading(16),
    labelLarge = Base.labelLarge.reading(16),
    labelMedium = Base.labelMedium.reading(16),
    labelSmall = Base.labelSmall.reading(16),
)

/** The same as [NewtTypography] since 2.0.0. */
@Deprecated("Same as NewtTypography since 2.0.0", ReplaceWith("NewtTypography"))
val NewtReadingTypography: Typography get() = NewtTypography

private fun TextStyle.reading(sp: Int) =
    copy(fontFamily = NewtFonts.Body, fontSize = sp.sp, lineHeight = (sp * 1.3f).sp)

/**
 * Shows [content] with the 16sp-floor text styles (in JetBrains Mono in the terminal look). Not
 * needed since 2.0.0: [NewtTheme] has the floor already.
 */
@Deprecated("Not needed since 2.0.0: NewtTheme has the 16sp floor")
@Composable
fun NewtReadingText(content: @Composable () -> Unit) {
    val typography = if (terminalLook()) terminalTypography(NewtTypography) else NewtTypography
    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = typography, content = content)
}

@Composable
fun NewtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) NewtNight else NewtDay,
        typography = NewtTypography,
        content = content,
    )
}
