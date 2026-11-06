package io.github.secnewt.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyTest {

    private fun Typography.reading(): List<Pair<String, TextStyle>> = listOf(
        "titleLarge" to titleLarge, "titleMedium" to titleMedium, "titleSmall" to titleSmall,
        "bodyLarge" to bodyLarge, "bodyMedium" to bodyMedium, "bodySmall" to bodySmall,
        "labelLarge" to labelLarge, "labelMedium" to labelMedium, "labelSmall" to labelSmall,
    )

    @Test
    fun floorSizes() {
        with(NewtTypography) {
            for (style in listOf(bodySmall, labelSmall, labelMedium, bodyMedium, labelLarge)) assertEquals(16.sp, style.fontSize)
            assertEquals(17.sp, bodyLarge.fontSize)
            assertEquals(17.sp, titleSmall.fontSize)
            assertEquals(19.sp, titleMedium.fontSize)
            assertEquals(21.sp, titleLarge.fontSize)
        }
    }

    @Test
    fun noReadingStyleUnder16InEitherLook() {
        for (typography in listOf(NewtTypography, terminalTypography(NewtTypography))) {
            for ((name, style) in typography.reading()) {
                assertTrue("$name is ${style.fontSize}", style.fontSize.value >= 16f)
            }
        }
    }

    @Test
    fun displayAndHeadlineUnchanged() {
        assertEquals(Typography().headlineSmall.fontSize, NewtTypography.headlineSmall.fontSize)
        assertEquals(Typography().displayLarge.fontSize, NewtTypography.displayLarge.fontSize)
    }
}
