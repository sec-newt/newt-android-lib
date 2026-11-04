package io.github.secnewt.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import io.github.secnewt.ui.newtStripe
import io.github.secnewt.ui.NewtTone
import io.github.secnewt.ui.NewtStateChip
import io.github.secnewt.ui.NewtSummary
import io.github.secnewt.ui.NewtPickList
import io.github.secnewt.ui.NewtChoice
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.Icons
import io.github.secnewt.ui.NewtValueRow
import io.github.secnewt.ui.NewtSwitchRow
import io.github.secnewt.ui.NewtSettingsCard
import io.github.secnewt.ui.NewtRuleHeading
import io.github.secnewt.ui.NewtReadingText
import io.github.secnewt.ui.NewtLockedRow
import io.github.secnewt.ui.NewtInfo
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
    private fun TerminalGallery() = NewtLookTheme(NewtLook.TERMINAL, host = "gallery") {
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
    /** The settings pieces (UI refresh), in reading text. */
    @Composable
    private fun SettingsGallery(transparent: Boolean = false) = NewtReadingText {
        Surface(color = if (transparent) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NewtRuleHeading("Today")
                NewtSettingsCard("Ring on silent", icon = Icons.Filled.Notifications) {
                    NewtSwitchRow("Favorites", checked = true, onCheckedChange = {}, hint = "At alarm volume")
                    NewtSwitchRow("Quiet hours", checked = false, onCheckedChange = {}, hint = "Only those two ring, at night")
                    NewtValueRow("Unknown callers", "Balanced", onClick = {})
                    NewtLockedRow("After 911", "Always", "Every call rings for an hour after you call 911.")
                    NewtInfo(details = "Numbers that look made up or copy yours.")
                }
            }
        }
    }

    @Test
    fun settingsGalleryDark() = composeRule.newtScreenshot("settings-gallery-dark", dark = true, minTextSp = 16f) { SettingsGallery() }

    @Test
    fun settingsGalleryLight() = composeRule.newtScreenshot("settings-gallery-light", dark = false, minTextSp = 16f) { SettingsGallery() }

    @Test
    fun settingsGallery200pct() = composeRule.newtScreenshot("settings-gallery-200pct", dark = true, fontScale = 2f) { SettingsGallery() }

    @Test
    fun settingsGalleryTerminalDark() = composeRule.newtScreenshot("settings-gallery-terminal-dark", dark = true, minTextSp = 16f) {
        NewtLookTheme(NewtLook.TERMINAL, host = "gallery") { SettingsGallery(transparent = true) }
    }
    /** Choices, chips, the summary box and the edge stripe (UI refresh), in reading text. */
    @Composable
    private fun ChoicesGallery(transparent: Boolean = false) = NewtReadingText {
        Surface(color = if (transparent) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NewtSummary("Right now", "Favorites ring on silent. Spam is watched, not blocked.")
                NewtChoice(listOf(true, false), true, label = { if (it) "Watch" else "Block" }, onSelect = {})
                NewtChoice(
                    listOf("Off", "Balanced", "Strict"), "Balanced", label = { it }, onSelect = {},
                    explanation = "Suspicious numbers are silenced. Your contacts always ring.",
                )
                NewtPickList(
                    listOf("Quiet note", "Like a text", "Don't notify"), "Quiet note", label = { it },
                    hint = { if (it == "Quiet note") "\"Becca loved your message\"" else null }, onSelect = {},
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NewtStateChip("Ring", NewtTone.Good)
                    NewtStateChip("Silence", NewtTone.Info)
                    NewtStateChip("Block", NewtTone.Danger)
                    NewtStateChip("2", NewtTone.Neutral)
                }
                Text(
                    "Likely spam · missed 5:00 PM",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.newtStripe(MaterialTheme.colorScheme.error).padding(12.dp),
                )
            }
        }
    }

    @Test
    fun choicesGalleryDark() = composeRule.newtScreenshot("choices-gallery-dark", dark = true, minTextSp = 16f) { ChoicesGallery() }

    @Test
    fun choicesGalleryLight() = composeRule.newtScreenshot("choices-gallery-light", dark = false, minTextSp = 16f) { ChoicesGallery() }

    @Test
    fun choicesGallery200pct() = composeRule.newtScreenshot("choices-gallery-200pct", dark = true, fontScale = 2f) { ChoicesGallery() }

    @Test
    fun choicesGalleryTerminalDark() = composeRule.newtScreenshot("choices-gallery-terminal-dark", dark = true, minTextSp = 16f) {
        NewtLookTheme(NewtLook.TERMINAL, host = "gallery") { ChoicesGallery(transparent = true) }
    }
}
