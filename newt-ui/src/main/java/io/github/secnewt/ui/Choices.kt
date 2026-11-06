package io.github.secnewt.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A state as a word in a colored outline, like "Balanced" or "Would silence". Always has words. */
@Composable
fun NewtStateChip(text: String, tone: NewtTone, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val color = tone.color()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .border(2.5.dp, color, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text, color = color, fontFamily = NewtFonts.Mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

/**
 * Two or three choices as one row of big buttons; the chosen one is filled. [explanation]
 * (about the chosen option) goes under it. Nothing is filled when [selected] is null (for
 * example a "custom" mix that matches no preset).
 */
@Composable
fun <T> NewtChoice(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    explanation: String? = null,
) {
    require(options.size in 2..3) { "NewtChoice takes 2 or 3 options; use NewtPickList for more" }
    val shape = RoundedCornerShape(16.dp)
    val style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    val measurer = rememberTextMeasurer()
    val divider = MaterialTheme.colorScheme.outlineVariant
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // Side by side only while every label fits on one line; with big text they stack.
            val room = with(LocalDensity.current) {
                ((maxWidth - 2.dp * (options.size - 1)) / options.size - 12.dp).roundToPx()
            }
            val sideBySide = options.all { measurer.measure(label(it), style, softWrap = false).size.width <= room }
            val segment = @Composable { option: T, segmentModifier: Modifier ->
                val chosen = option == selected
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = segmentModifier
                        .heightIn(min = 54.dp)
                        .then(if (chosen) Modifier.background(newtGradient()) else Modifier)
                        .selectable(selected = chosen, role = Role.RadioButton, onClick = { onSelect(option) })
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = label(option),
                        style = style,
                        textAlign = TextAlign.Center,
                        color = if (chosen) onNewtGradient() else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val frame = Modifier
                .fillMaxWidth()
                .clip(shape)
                .border(2.5.dp, MaterialTheme.colorScheme.primary, shape)
                .selectableGroup()
            if (sideBySide) {
                Row(frame.height(IntrinsicSize.Min)) {
                    options.forEachIndexed { index, option ->
                        if (index > 0) Box(Modifier.width(2.dp).fillMaxHeight().background(divider))
                        segment(option, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            } else {
                Column(frame) {
                    options.forEachIndexed { index, option ->
                        if (index > 0) Box(Modifier.height(2.dp).fillMaxWidth().background(divider))
                        segment(option, Modifier.fillMaxWidth())
                    }
                }
            }
        }
        if (explanation != null) {
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Four or more choices (or ones that need a hint): rows with a big check; only the chosen row shows its hint. */
@Composable
fun <T> NewtPickList(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    hint: (T) -> String?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val line = MaterialTheme.colorScheme.outlineVariant
    Column(modifier.selectableGroup()) {
        options.forEach { option ->
            val chosen = option == selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind { drawRect(line, size = size.copy(height = 2.dp.toPx())) }
                    .then(if (chosen) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) else Modifier)
                    .selectable(selected = chosen, role = Role.RadioButton, onClick = { onSelect(option) })
                    .heightIn(min = 58.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .then(
                            if (chosen) {
                                Modifier.background(newtGradient())
                            } else {
                                Modifier.border(3.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                            },
                        ),
                ) {
                    if (chosen) Icon(Icons.Filled.Check, contentDescription = null, tint = onNewtGradient(), modifier = Modifier.size(20.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label(option), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    val h = hint(option)
                    if (chosen && h != null) {
                        Text(h, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** The "RIGHT NOW" box: one plain sentence about what's happening, read as one thing. */
@Composable
fun NewtSummary(label: String, text: String, modifier: Modifier = Modifier, tone: NewtTone = NewtTone.Good) {
    val color = tone.color()
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .newtStripe(color, tint = false)
            .clearAndSetSemantics { contentDescription = "$label. $text" }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = color,
            fontFamily = NewtFonts.Mono,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.12.em,
        )
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * A 6dp colored bar along the leading edge, with a faint wash of the color behind the content.
 * Decoration only: whatever the color means must also be said in words.
 */
fun Modifier.newtStripe(color: Color, tint: Boolean = true): Modifier = this
    .drawBehind {
        if (tint) {
            drawRect(Brush.horizontalGradient(0f to color.copy(alpha = 0.13f), 0.7f to Color.Transparent))
        }
        val bar = 6.dp.toPx()
        val x = if (layoutDirection == LayoutDirection.Rtl) size.width - bar else 0f
        drawRect(color, topLeft = Offset(x, 0f), size = Size(bar, size.height))
    }
    .padding(start = 6.dp)
