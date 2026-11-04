package io.github.secnewt.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class TextSizeCheckTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun smallTextFails() {
        val error = assertThrows(AssertionError::class.java) {
            composeRule.newtScreenshot("t-small", dark = true, minTextSp = 16f) { Text("tiny", fontSize = 12.sp) }
        }
        assertTrue(error.message, error.message!!.contains("\"tiny\" is 12"))
    }

    @Test
    fun bigTextPasses() = composeRule.newtScreenshot("t-big", dark = true, minTextSp = 16f) {
        Column {
            Text("sixteen", fontSize = 16.sp)
            Text("twenty-one", fontSize = 21.sp)
        }
    }

    @Test
    fun spanCounts() {
        assertThrows(AssertionError::class.java) {
            composeRule.newtScreenshot("t-span", dark = true, minTextSp = 16f) {
                Text(
                    buildAnnotatedString {
                        append("big ")
                        withStyle(SpanStyle(fontSize = 12.sp)) { append("small") }
                    },
                    fontSize = 18.sp,
                )
            }
        }
    }

    @Test
    fun skippedAtBigFontScale() = composeRule.newtScreenshot("t-200", dark = true, fontScale = 2f, minTextSp = 16f) {
        Text("tiny but scaled", fontSize = 12.sp)
    }
}
