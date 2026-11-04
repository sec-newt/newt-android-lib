package io.github.secnewt.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.secnewt.ui.NewtInfo
import io.github.secnewt.ui.NewtLockedRow
import io.github.secnewt.ui.NewtReadingText
import io.github.secnewt.ui.NewtSwitchRow
import io.github.secnewt.ui.NewtTheme
import io.github.secnewt.ui.NewtValueRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Behavior and screen-reader checks for the shared pieces (pictures are in GalleryTest). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class PiecesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(fontScale: Float = 1f, content: @Composable () -> Unit) = composeRule.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            NewtTheme(darkTheme = true) { NewtReadingText(content) }
        }
    }

    private fun overflows(text: String): Boolean {
        val node = composeRule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode()
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        // Compose's own hasVisualOverflow reports false alarms for wrapped text here, so look for
        // what overflow really looks like: one-line text wider than its box (clipped), a word
        // split across lines because it didn't fit, lines cut off at the bottom, or a "…".
        val r = results.single()
        val last = r.lineCount - 1
        val clipped = !r.layoutInput.softWrap && r.multiParagraph.intrinsics.maxIntrinsicWidth > r.size.width + 0.5f
        val splitWord = (0 until last).any {
            val end = r.getLineEnd(it)
            end in 1 until text.length && !text[end - 1].isWhitespace() && !text[end].isWhitespace()
        }
        val tooTall = r.getLineBottom(last) > r.size.height + 0.5f
        return clipped || splitWord || tooTall || r.isLineEllipsized(last)
    }

    @Test
    fun switchRowReadsTitleAndStateOnce() {
        show { NewtSwitchRow("Favorites", checked = true, onCheckedChange = {}) }
        composeRule.onNodeWithText("Favorites")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            .assertIsOn()
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ToggleableState), useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun switchRowToggles() {
        var got: Boolean? = null
        show { NewtSwitchRow("Favorites", checked = true, onCheckedChange = { got = it }) }
        composeRule.onNodeWithText("Favorites").performClick()
        assertEquals(false, got)
    }

    @Test
    fun valueRowIsButtonWithValue() {
        show { NewtValueRow("Unknown callers", "Balanced", onClick = {}) }
        composeRule.onNode(hasText("Unknown callers") and hasText("Balanced"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun lockedRowIsNotClickable() {
        show { NewtLockedRow("After 911", "Always", "Every call rings for an hour after you call 911.") }
        composeRule.onNodeWithContentDescription("After 911, Always. Every call rings for an hour after you call 911.")
            .assertHasNoClickAction()
    }

    @Test
    fun infoExpands() {
        show { NewtInfo(details = "Numbers that look made up.") }
        composeRule.onNodeWithText("Numbers that look made up.").assertDoesNotExist()
        composeRule.onNodeWithText("What's this?").performClick()
        composeRule.onNodeWithText("Numbers that look made up.").assertIsDisplayed()
    }

    @Test
    fun overflowCheckCatchesClippedText() {
        show { Box(Modifier.width(40.dp)) { Text("Unknown callers", softWrap = false, maxLines = 1) } }
        assertTrue(overflows("Unknown callers"))
    }

    @Test
    fun titleNeverOverflowsAt200pct() {
        show(fontScale = 2f) {
            Box(Modifier.width(340.dp)) {
                Column {
                    NewtValueRow("Unknown callers", "Balanced", onClick = {})
                    NewtSwitchRow("Quiet in Do Not Disturb", checked = false, onCheckedChange = {})
                }
            }
        }
        assertFalse("Unknown callers overflows", overflows("Unknown callers"))
        assertFalse("Quiet in Do Not Disturb overflows", overflows("Quiet in Do Not Disturb"))
    }
}
