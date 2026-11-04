package io.github.secnewt.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TonesTest {

    private val schemes = listOf(NewtNight to true, NewtDay to false)

    private fun Typography.all(): List<Pair<String, TextStyle>> = listOf(
        "displayLarge" to displayLarge, "displayMedium" to displayMedium, "displaySmall" to displaySmall,
        "headlineLarge" to headlineLarge, "headlineMedium" to headlineMedium, "headlineSmall" to headlineSmall,
        "titleLarge" to titleLarge, "titleMedium" to titleMedium, "titleSmall" to titleSmall,
        "bodyLarge" to bodyLarge, "bodyMedium" to bodyMedium, "bodySmall" to bodySmall,
        "labelLarge" to labelLarge, "labelMedium" to labelMedium, "labelSmall" to labelSmall,
    )

    @Test
    fun everyToneIsSevenToOneInBothSchemes() {
        for ((scheme, dark) in schemes) {
            for (tone in NewtTone.entries) {
                val c = toneColor(tone, scheme, dark)
                for (bg in listOf(scheme.background, scheme.surfaceContainerLow)) {
                    val ratio = contrastRatio(c, bg)
                    assertTrue("$tone on ${if (dark) "dark" else "light"} is $ratio:1", ratio >= 7f)
                }
            }
        }
    }

    @Test
    fun textOnGradientIsSevenToOne() {
        for ((scheme, dark) in schemes) {
            val on = onGradient(dark)
            for (stop in listOf(scheme.primary, scheme.secondary)) {
                val ratio = contrastRatio(on, stop)
                assertTrue("text on gradient (${if (dark) "dark" else "light"}) is $ratio:1", ratio >= 7f)
            }
        }
    }

    @Test
    fun readingTypographyFloor() {
        for ((name, style) in NewtReadingTypography.all()) {
            assertTrue("$name is ${style.fontSize}", style.fontSize.value >= 16f)
        }
        assertEquals(21.sp, NewtReadingTypography.titleLarge.fontSize)
        assertEquals(17.sp, NewtReadingTypography.bodyLarge.fontSize)
    }

    @Test
    fun terminalClampKeepsOldSizes() {
        val base = NewtTypography.all().toMap()
        for ((name, style) in terminalTypography(NewtTypography).all()) {
            assertEquals(name, base.getValue(name).fontSize.value * 0.94f, style.fontSize.value, 0.001f)
        }
    }

    @Test
    fun terminalClampHoldsFloor() {
        for ((name, style) in terminalTypography(NewtReadingTypography).all()) {
            assertTrue("$name is ${style.fontSize}", style.fontSize.value >= 16f)
        }
    }

}
