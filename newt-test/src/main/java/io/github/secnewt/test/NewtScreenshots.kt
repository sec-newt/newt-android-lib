package io.github.secnewt.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
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
    minTextSp: Float? = 16f,
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
    // Sizes are only meaningful at the phone's default font size.
    if (minTextSp != null && fontScale == 1f) assertTextAtLeast(minTextSp)
}

/**
 * Fails if any text on screen is smaller than [minSp] (counting styled parts of a text too),
 * listing every offender. It also sees text hidden from screen readers. Text drawn as a
 * picture isn't seen.
 */
fun ComposeContentTestRule.assertTextAtLeast(minSp: Float) {
    waitForIdle()
    val problems = mutableListOf<String>()
    fun check(text: String, sizes: List<TextUnit>) {
        for (size in sizes) {
            when {
                size == TextUnit.Unspecified -> Unit
                size.isEm -> problems += "\"$text\" uses em; use sp"
                size.value < minSp - 0.01f -> problems += "\"$text\" is ${size.value}sp, needs ${minSp}sp"
            }
        }
    }
    // Text that screen readers can reach reports its laid-out style.
    val nodes = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
        .fetchSemanticsNodes()
    for (node in nodes) {
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        for (result in results) {
            val input = result.layoutInput
            check(input.text.text, listOf(input.style.fontSize) + input.text.spanStyles.map { it.item.fontSize })
        }
    }
    // Text hidden from screen readers (clearAndSetSemantics) has no semantics node, so walk the
    // layout tree too and read each text's style from its modifier.
    val root = onRoot(useUnmergedTree = true).fetchSemanticsNode().layoutInfo
    for (info in layoutTree(root)) {
        for (modifier in info.getModifierInfo().map { it.modifier }) {
            val found = textOf(modifier) ?: continue
            check(found.first, found.second)
        }
    }
    if (problems.isNotEmpty()) throw AssertionError("Text too small:\n" + problems.distinct().joinToString("\n"))
}

// Compose keeps a layout node's children internal; its JVM getter name is stable across releases.
private fun layoutTree(start: LayoutInfo): List<LayoutInfo> {
    var top = start
    while (top.parentInfo != null) top = top.parentInfo!!
    val out = mutableListOf<LayoutInfo>()
    val stack = ArrayDeque(listOf(top))
    while (stack.isNotEmpty()) {
        val info = stack.removeLast()
        out += info
        val getter = info.javaClass.methods.firstOrNull { it.name.startsWith("getChildren") && it.parameterCount == 0 && List::class.java.isAssignableFrom(it.returnType) }
            ?: error("Can't walk the layout tree: Compose renamed LayoutNode.children")
        @Suppress("UNCHECKED_CAST")
        (getter.invoke(info) as List<LayoutInfo>).forEach { stack.addLast(it) }
    }
    return out
}

/** The text and font sizes of a Text's modifier element (Compose's text elements are internal). */
private fun textOf(modifier: Modifier): Pair<String, List<TextUnit>>? {
    val name = modifier.javaClass.simpleName
    if (!name.startsWith("Text") || !name.endsWith("Element")) return null
    fun field(n: String) = modifier.javaClass.declaredFields.firstOrNull { it.name == n }?.apply { isAccessible = true }?.get(modifier)
    val style = field("style") as? TextStyle ?: return null
    return when (val text = field("text")) {
        is AnnotatedString -> text.text to (listOf(style.fontSize) + text.spanStyles.map { it.item.fontSize })
        is String -> text to listOf(style.fontSize)
        else -> null
    }
}
