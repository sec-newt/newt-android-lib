package io.github.secnewt.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityCheckOptions
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityChecker
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.checkRoboAccessibility
import io.github.secnewt.ui.NewtTheme

/**
 * Shows [content] in the Newt theme, saves a screenshot to build/screenshots/[name].png
 * (CI uploads these for review), and fails on any accessibility warning: missing labels,
 * small touch targets, low contrast.
 *
 * Call it from a test class set up for Robolectric, like this:
 *
 *     @RunWith(AndroidJUnit4::class)
 *     @GraphicsMode(GraphicsMode.Mode.NATIVE)
 *     @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
 *     class MyScreenTest {
 *         @get:Rule val composeRule = createComposeRule()
 *
 *         @Test fun dark() = composeRule.newtScreenshot("my-screen-dark", dark = true) { MyScreen() }
 *         @Test fun bigText() = composeRule.newtScreenshot("my-screen-200pct", dark = true, fontScale = 2f) { MyScreen() }
 *     }
 *
 * [theme] lets an app wrap its own theme around the content instead (Newt Home's terminal look).
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.newtScreenshot(
    name: String,
    dark: Boolean,
    fontScale: Float = 1f,
    theme: @Composable (dark: Boolean, content: @Composable () -> Unit) -> Unit = { d, c -> NewtTheme(darkTheme = d, content = c) },
    content: @Composable () -> Unit,
) {
    setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            theme(dark, content)
        }
    }
    onRoot().captureRoboImage("build/screenshots/$name.png")
    onRoot().checkRoboAccessibility(
        RoborazziATFAccessibilityCheckOptions(
            failureLevel = RoborazziATFAccessibilityChecker.CheckLevel.Warning,
        )
    )
}
