package io.github.secnewt.ui

import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat

/**
 * The family look: Newt Home's `look terminal` / `look clean` setting, followed by every Newt
 * app. Newt Home is the one switch; it shares the setting (read-only) with apps signed with
 * the Newt key, through a signature permission nobody is asked for.
 */
enum class NewtLook {
    TERMINAL,
    CLEAN,
    ;

    companion object {
        /** A screen title as a terminal command: "Spam protection" → "spam-protection". */
        fun command(title: String): String =
            title.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    }
}

/** The app's name in terminal lines (`newt@phone:~$`); set by [NewtLookTheme]. */
val LocalNewtHost = staticCompositionLocalOf { "newt" }

/** The look in force. Apps provide it with [NewtLookTheme]; anything else sees the clean look. */
val LocalNewtLook = staticCompositionLocalOf { NewtLook.CLEAN }

/** Whether the terminal look is on. */
@Composable
@ReadOnlyComposable
fun terminalLook(): Boolean = LocalNewtLook.current == NewtLook.TERMINAL

/**
 * Reads Newt Home's look. Apps need `<uses-permission android:name="{PERMISSION}" />` and a
 * `<queries><provider android:authorities="io.github.secnewt.home.look" /></queries>`.
 */
object NewtFamilyLook {
    const val NEWT_HOME = "io.github.secnewt.home"
    const val PERMISSION = "$NEWT_HOME.permission.READ_LOOK"
    const val AUTHORITY = "$NEWT_HOME.look"
    const val COLUMN = "look"
    val URI: Uri by lazy { Uri.parse("content://$AUTHORITY/look") }

    /** Newt Home's look, or null when Newt Home isn't installed or isn't sharing it. */
    fun read(context: Context): NewtLook? {
        if (context.checkSelfPermission(PERMISSION) != android.content.pm.PackageManager.PERMISSION_GRANTED) return null
        return try {
            context.contentResolver.query(URI, null, null, null, null)?.use { c ->
                if (!c.moveToFirst()) return null
                val name = c.getString(c.getColumnIndexOrThrow(COLUMN))
                NewtLook.entries.firstOrNull { it.name == name }
            }
        } catch (e: RuntimeException) {
            null
        }
    }
}

/**
 * Newt Home's look, kept up to date while shown (Newt Home says when it changes). Clean when
 * Newt Home isn't there, so apps look as they always have.
 */
@Composable
fun rememberFamilyLook(): NewtLook {
    val context = LocalContext.current
    var look by remember { mutableStateOf(NewtFamilyLook.read(context) ?: NewtLook.CLEAN) }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                look = NewtFamilyLook.read(context) ?: NewtLook.CLEAN
            }
        }
        val watching = try {
            context.contentResolver.registerContentObserver(NewtFamilyLook.URI, false, observer)
            true
        } catch (e: RuntimeException) {
            false // Newt Home isn't installed
        }
        onDispose { if (watching) context.contentResolver.unregisterContentObserver(observer) }
    }
    return look
}

/**
 * Wrap an app's screens in this (inside [NewtTheme]). In the terminal look every text style
 * switches to JetBrains Mono and the scanlines and ASCII newt sit behind everything; screens
 * using [Scaffold] should pass `containerColor = newtContainerColor()` so they show through.
 */
@Composable
fun NewtLookTheme(look: NewtLook, host: String = "newt", content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNewtLook provides look, LocalNewtHost provides host) {
        if (look == NewtLook.CLEAN) {
            content()
        } else {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = terminalTypography(MaterialTheme.typography)) {
                // The theme's own background first, then the gadget, then the screen (whose
                // background is see-through, so the gadget shows): never relies on the window.
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    NewtGadgetBackground()
                    // A see-through screen has no text color of its own (Android falls back to
                    // black), so give it the theme's.
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                        content()
                    }
                }
            }
        }
    }
}

/** A screen's background: see-through in the terminal look so the gadget shows behind it. */
@Composable
@ReadOnlyComposable
fun newtContainerColor(): Color = if (terminalLook()) Color.Transparent else MaterialTheme.colorScheme.background

/** The terminal's green `~$` prompt (also "good"), at 7:1 or better. */
object NewtTerminal {
    @Composable
    fun prompt(): Color = if (MaterialTheme.colorScheme.background.lightness() < 0.5f) Color(0xFF9ECE6A) else Color(0xFF2D5A0A)
}

/** Every style in JetBrains Mono, which runs wider, so a touch smaller to fit the same lines. */
private fun terminalTypography(base: Typography): Typography {
    fun TextStyle.mono() = copy(fontFamily = NewtFonts.Mono, fontSize = fontSize * 0.94f, lineHeight = lineHeight * 0.94f)
    return Typography(
        displayLarge = base.displayLarge.mono(), displayMedium = base.displayMedium.mono(), displaySmall = base.displaySmall.mono(),
        headlineLarge = base.headlineLarge.mono(), headlineMedium = base.headlineMedium.mono(), headlineSmall = base.headlineSmall.mono(),
        titleLarge = base.titleLarge.mono(), titleMedium = base.titleMedium.mono(), titleSmall = base.titleSmall.mono(),
        bodyLarge = base.bodyLarge.mono(), bodyMedium = base.bodyMedium.mono(), bodySmall = base.bodySmall.mono(),
        labelLarge = base.labelLarge.mono(), labelMedium = base.labelMedium.mono(), labelSmall = base.labelSmall.mono(),
    )
}

/**
 * `newt@weather:~$ forecast --7d`: a terminal line above a section in the terminal look. In
 * the clean look it shows [title] as a heading instead (or nothing if there's none). The
 * terminal line is for the look only, so screen readers hear [title] either way.
 */
@Composable
fun NewtPromptLine(host: String, command: String, title: String?, modifier: Modifier = Modifier) {
    if (!terminalLook()) {
        if (title != null) SectionHeading(title, modifier)
        return
    }
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = NewtTerminal.prompt(), fontWeight = FontWeight.Bold)) { append("newt@$host:~$ ") }
            append(command)
        },
        fontFamily = NewtFonts.Mono,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(top = 8.dp)
            .then(if (title != null) Modifier.clearAndSetSemantics { heading(); this.contentDescription = title } else Modifier.clearAndSetSemantics { }),
    )
}

private val ASCII_NEWT = """
          .-~~-.
         ( o  o )
          \ \/ /
     .-----'  '-----.
    /  /|        |\  \
   '--' |        | '--'
        |        |
     .--'        '--.
    /  /|        |\  \
   '--' \        / '--'
         \      /
          \    /
           |  |
           |  |
            \  \
             '. '.
               '~~
""".trimIndent()

/**
 * The ASCII newt as a picture rather than text. Android's "High contrast text" setting
 * redraws all text at full strength with dark boxes behind it, which would make a dim
 * background newt bright; a picture is left alone. Drawn in white; tinted when shown.
 */
private fun renderNewt(context: Context, density: Float): ImageBitmap {
    val font = ResourcesCompat.getFont(context, io.github.secnewt.ui.R.font.jetbrains_mono)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(font, 700, false)
        textSize = 19f * density
        color = android.graphics.Color.WHITE
    }
    val lines = ASCII_NEWT.lines()
    val lineHeight = 20f * density
    val width = lines.maxOf { paint.measureText(it) }.toInt() + 1
    val height = (lines.size * lineHeight).toInt() + 1
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    lines.forEachIndexed { i, line -> canvas.drawText(line, 0f, (i + 0.8f) * lineHeight, paint) }
    return bitmap.asImageBitmap()
}


/**
 * Scanlines across the whole screen, a soft darkening at the edges, and the ASCII newt in
 * the middle. It's drawn, not laid out, so screen readers never see it, and its size stays
 * the same at any text size.
 */
@Composable
fun NewtGadgetBackground(modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background.lightness() < 0.5f
    val line = if (dark) Color(0xFFAABEFF).copy(alpha = 0.045f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    val newt = MaterialTheme.colorScheme.primary.copy(alpha = if (dark) 0.13f else 0.07f)
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val picture = remember(density) { renderNewt(context, density) }
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                val gap = 4.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += gap
                }
                if (dark) {
                    drawRect(
                        Brush.radialGradient(
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.35f),
                            center = Offset(size.width / 2, size.height * 0.4f),
                            radius = size.maxDimension * 0.75f,
                        ),
                    )
                }
                drawImage(
                    picture,
                    topLeft = Offset(
                        (size.width - picture.width) / 2,
                        (size.height - picture.height) / 2 + 30.dp.toPx(),
                    ),
                    colorFilter = ColorFilter.tint(newt),
                )
            },
    )
}


private fun Color.lightness(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
