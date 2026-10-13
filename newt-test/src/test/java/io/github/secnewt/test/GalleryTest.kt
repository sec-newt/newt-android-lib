package io.github.secnewt.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.secnewt.ui.CardTitle
import io.github.secnewt.ui.NewtFonts
import io.github.secnewt.ui.NewtLook
import io.github.secnewt.ui.NewtLookTheme
import io.github.secnewt.ui.NewtPromptLine
import io.github.secnewt.ui.NewtWeather
import io.github.secnewt.ui.ScreenHeader
import io.github.secnewt.ui.SectionHeading
import io.github.secnewt.ui.SectionLabel
import io.github.secnewt.ui.StarOutline
import io.github.secnewt.ui.TabHeader
import io.github.secnewt.ui.neonFieldColors
import io.github.secnewt.ui.neonOutline
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Every shared piece on one screen. A change to the library shows up in these screenshots
 * before any app sees it.
 */
private val CONDITIONS = listOf("CLEAR", "PARTLY_CLOUDY", "CLOUDY", "RAIN", "THUNDERSTORM", "SNOW", "FOG", "WIND")

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class GalleryTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Composable
    private fun Gallery(transparent: Boolean = false) {
        Surface(color = if (transparent) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TabHeader("Tab title", onOpenSettings = {})
                ScreenHeader("Screen title", onBack = {})
                SectionHeading("Section heading")
                SectionLabel("Section label")
                Card(
                    border = neonOutline(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CardTitle("Card title")
                        Text("Reading text in Atkinson Hyperlegible.", style = MaterialTheme.typography.bodyLarge)
                        Text("555 0142 290", fontFamily = NewtFonts.Mono, style = MaterialTheme.typography.titleLarge)
                    }
                }
                OutlinedButton(onClick = {}, border = neonOutline()) {
                    Icon(StarOutline, contentDescription = null)
                    Text("  Outlined button")
                }
                OutlinedTextField(
                    value = "Text field",
                    onValueChange = {},
                    label = { Text("Label") },
                    colors = neonFieldColors(),
                )
                // Weather: every icon by day, sun and moon variants by night, and high/low colors.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CONDITIONS.forEach { WeatherIcon(it, isDaytime = true) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CLEAR", "PARTLY_CLOUDY").forEach { WeatherIcon(it, isDaytime = false) }
                    Text("↑ High 90°", color = NewtWeather.highColor(), fontFamily = NewtFonts.Mono, style = MaterialTheme.typography.titleLarge)
                    Text("↓ Low 75°", color = NewtWeather.lowColor(), fontFamily = NewtFonts.Mono, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }

    @Composable
    private fun WeatherIcon(condition: String, isDaytime: Boolean) = Icon(
        painter = painterResource(NewtWeather.icon(condition, isDaytime)),
        contentDescription = NewtWeather.words(condition),
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp),
    )

    /** The terminal look: the same pieces in JetBrains Mono over scanlines and the ASCII newt. */
    @Composable
    private fun TerminalGallery() = NewtLookTheme(NewtLook.TERMINAL) {
        Column {
            NewtPromptLine("gallery", "ls --all", title = "Gallery", modifier = Modifier.padding(horizontal = 16.dp))
            Gallery(transparent = true)
        }
    }

    @Test
    fun galleryTerminalDark() = composeRule.newtScreenshot("gallery-terminal-dark", dark = true) { TerminalGallery() }

    @Test
    fun galleryTerminalLight() = composeRule.newtScreenshot("gallery-terminal-light", dark = false) { TerminalGallery() }

    @Test
    fun galleryDark() = composeRule.newtScreenshot("gallery-dark", dark = true) { Gallery() }

    @Test
    fun galleryLight() = composeRule.newtScreenshot("gallery-light", dark = false) { Gallery() }

    @Test
    fun galleryLargeText() = composeRule.newtScreenshot("gallery-200pct", dark = true, fontScale = 2f) { Gallery() }
}
