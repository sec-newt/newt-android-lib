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
import io.github.secnewt.ui.NewtFonts
import io.github.secnewt.ui.NewtLook
import io.github.secnewt.ui.NewtLookTheme
import io.github.secnewt.ui.NewtSettingsCard
import androidx.compose.ui.test.assertHeightIsAtLeast
import io.github.secnewt.ui.NewtStepState
import io.github.secnewt.ui.NewtStep
import io.github.secnewt.ui.NewtSteps
import io.github.secnewt.ui.NewtAction
import io.github.secnewt.ui.NewtActionGrid
import io.github.secnewt.ui.NewtPrimaryButton
import io.github.secnewt.ui.NewtButton
import org.junit.Assert.assertThrows
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import io.github.secnewt.ui.NewtTone
import io.github.secnewt.ui.NewtStateChip
import io.github.secnewt.ui.NewtSummary
import io.github.secnewt.ui.NewtPickList
import io.github.secnewt.ui.NewtChoice
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
    private enum class Level { Off, Balanced, Strict }

    @Test
    fun choiceSelectsAndReadsAsRadio() {
        var picked: Level? = null
        show { NewtChoice(Level.entries, Level.Balanced, label = { it.name }, onSelect = { picked = it }) }
        for (level in Level.entries) {
            val node = composeRule.onNodeWithText(level.name)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            if (level == Level.Balanced) node.assertIsSelected() else node.assertIsNotSelected()
        }
        composeRule.onNodeWithText("Strict").performClick()
        assertEquals(Level.Strict, picked)
    }

    @Test
    fun choiceNeverSplitsALabelAt200pct() {
        show(fontScale = 2f) {
            Box(Modifier.width(340.dp)) { NewtChoice(Level.entries, Level.Balanced, label = { it.name }, onSelect = {}) }
        }
        for (level in Level.entries) assertFalse("${level.name} overflows", overflows(level.name))
    }

    @Test
    fun choiceWithNothingSelected() {
        show { NewtChoice(Level.entries, null, label = { it.name }, onSelect = {}) }
        for (level in Level.entries) composeRule.onNodeWithText(level.name).assertIsNotSelected()
    }

    @Test
    fun choiceRejectsFourOptions() {
        assertThrows(IllegalArgumentException::class.java) {
            show { NewtChoice(listOf(1, 2, 3, 4), 1, label = { "$it" }, onSelect = {}) }
        }
    }

    @Test
    fun pickListShowsOnlySelectedHint() {
        show {
            NewtPickList(
                options = Level.entries,
                selected = Level.Balanced,
                label = { it.name },
                hint = { "${it.name} hint" },
                onSelect = {},
            )
        }
        composeRule.onNodeWithText("Balanced hint", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Off hint", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText("Strict hint", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun summaryReadsAsOneSentence() {
        show { NewtSummary("Right now", "Favorites ring on silent.") }
        composeRule.onNodeWithContentDescription("Right now. Favorites ring on silent.").assertExists()
    }

    @Test
    fun chipNeverEllipsizes() {
        // Half a phone row: the narrowest place a chip sits. It has to wrap between words, not split one.
        show(fontScale = 2f) { Box(Modifier.width(200.dp)) { NewtStateChip("Would silence", NewtTone.Danger) } }
        assertFalse(overflows("Would silence"))
    }
    @Test
    fun buttonsAreTallEnough() {
        show {
            Column {
                NewtButton("Test the voice", onClick = {})
                NewtPrimaryButton("Directions", onClick = {}, tone = NewtTone.Good, large = true)
            }
        }
        composeRule.onNodeWithText("Test the voice").assertHeightIsAtLeast(58.dp)
        composeRule.onNodeWithText("Directions").assertHeightIsAtLeast(64.dp)
    }

    @Test
    fun actionGridTilesAreButtons() {
        var tapped = ""
        show {
            NewtActionGrid(
                listOf(
                    NewtAction("Move up", onClick = { tapped = "up" }),
                    NewtAction("Hide", onClick = { tapped = "hide" }, hint = "Search still finds it"),
                ),
            )
        }
        composeRule.onNode(hasText("Hide") and hasText("Search still finds it"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()
        assertEquals("hide", tapped)
    }

    private val steps = listOf(
        NewtStep("Install a map app", NewtStepState.Done),
        NewtStep("Turn on link opening", NewtStepState.Now, action = { NewtPrimaryButton("Turn on", onClick = {}) }),
        NewtStep("Try it", NewtStepState.ToDo, action = { NewtPrimaryButton("Never shown", onClick = {}) }),
    )

    @Test
    fun stepsReadTheirState() {
        show { NewtSteps(steps) }
        composeRule.onNodeWithContentDescription("Step 1 of 3, Install a map app, done").assertExists()
        composeRule.onNodeWithContentDescription("Step 2 of 3, Turn on link opening, current").assertExists()
        composeRule.onNodeWithContentDescription("Step 3 of 3, Try it, not yet").assertExists()
    }

    @Test
    fun onlyCurrentStepShowsAction() {
        show { NewtSteps(steps) }
        composeRule.onNodeWithText("Turn on").assertIsDisplayed()
        composeRule.onNodeWithText("Never shown").assertDoesNotExist()
    }
    @Test
    fun cardHeadingUsesTerminalFontInTerminalLook() {
        composeRule.setContent {
            NewtTheme(darkTheme = true) {
                NewtLookTheme(NewtLook.TERMINAL) { NewtReadingText { NewtSettingsCard("Ring on silent") {} } }
            }
        }
        val node = composeRule.onNodeWithText("Ring on silent", useUnmergedTree = true).fetchSemanticsNode()
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        assertEquals(NewtFonts.Mono, results.single().layoutInput.style.fontFamily)
    }
}
