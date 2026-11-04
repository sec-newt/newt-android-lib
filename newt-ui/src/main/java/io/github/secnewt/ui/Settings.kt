package io.github.secnewt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/*
 * The shared settings layout (UI refresh): cards of rows, every row reading
 * title → one line of help → its state as a word. See NEWT_STYLE.md.
 */

/**
 * The switch picture, with ON or OFF written inside. Picture only: the row around it is the
 * switch for touch and screen readers ([NewtSwitchRow]). It grows with the phone's font size,
 * up to one and a half times.
 */
@Composable
fun NewtSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val fontScale = LocalDensity.current.fontScale
    val grow = fontScale.coerceIn(1f, 1.5f)
    val onColor = onNewtGradient()
    val offColor = MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(50)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (checked) Arrangement.End else Arrangement.Start,
        modifier = modifier
            .clearAndSetSemantics { }
            .size(width = 80.dp * grow, height = 40.dp * grow)
            .clip(shape)
            .then(
                if (checked) Modifier.background(newtGradient()) else Modifier.border(2.5.dp, offColor, shape),
            )
            .padding(horizontal = 5.dp * grow),
    ) {
        val thumb = @Composable {
            Box(Modifier.size(30.dp * grow).clip(CircleShape).background(if (checked) onColor else offColor))
        }
        val word = @Composable {
            Text(
                text = if (checked) "ON" else "OFF",
                color = if (checked) onColor else offColor,
                fontFamily = NewtFonts.Mono,
                fontWeight = FontWeight.Bold,
                // 16sp, grown like the track, whatever the phone's font size.
                fontSize = (16f * grow / fontScale).sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        if (checked) {
            word()
            thumb()
        } else {
            thumb()
            word()
        }
    }
}

/** A lifted card of setting rows with a heading. */
@Composable
fun NewtSettingsCard(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 11.dp, bottom = 9.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
            }
            Text(
                text = title,
                // Like CardTitle: JetBrains Mono in the terminal look.
                fontFamily = if (terminalLook()) NewtFonts.Mono else NewtFonts.Display,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.semantics { heading() },
            )
        }
        content()
    }
}

/**
 * The row every setting uses: a bold title, an optional one-line [hint], and its state at the
 * end. When the state doesn't leave the title at least half the width (big text), it moves
 * under the title instead of squashing it.
 */
@Composable
fun NewtSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val line = MaterialTheme.colorScheme.outlineVariant
    Layout(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind { drawRect(line, size = size.copy(height = 2.dp.toPx())) }
            .heightIn(min = 62.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (hint != null) {
                    Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) Box { trailing() }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val gap = 12.dp.roundToPx()
        val end = measurables.getOrNull(1)?.measure(loose)
        val sideBySide = end != null && constraints.maxWidth - end.width - gap >= constraints.maxWidth / 2
        val textWidth = if (end != null && sideBySide) constraints.maxWidth - end.width - gap else constraints.maxWidth
        val text = measurables[0].measure(loose.copy(maxWidth = textWidth))
        val height = when {
            end == null -> text.height
            sideBySide -> maxOf(text.height, end.height)
            else -> text.height + 8.dp.roundToPx() + end.height
        }.coerceAtLeast(constraints.minHeight)
        layout(constraints.maxWidth, height) {
            if (end == null) {
                text.place(0, (height - text.height) / 2)
            } else if (sideBySide) {
                text.place(0, (height - text.height) / 2)
                end.place(constraints.maxWidth - end.width, (height - end.height) / 2)
            } else {
                text.place(0, 0)
                end.place(0, text.height + 8.dp.roundToPx())
            }
        }
    }
}

/** A setting that's on or off. The whole row is the switch; it reads "title, on, switch". */
@Composable
fun NewtSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    NewtSettingRow(
        title = title,
        hint = hint,
        modifier = modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        trailing = { NewtSwitch(checked) },
    )
}

/** A setting with a current [value] (a chip) that opens a screen or a choice when tapped. */
@Composable
fun NewtValueRow(
    title: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    tone: NewtTone = NewtTone.Info,
) {
    NewtSettingRow(
        title = title,
        hint = hint,
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (value != null) NewtStateChip(value, tone)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    )
}

/** A setting that can't be changed (like call-backs after 911), saying why. */
@Composable
fun NewtLockedRow(title: String, value: String, reason: String, modifier: Modifier = Modifier) {
    val good = NewtTone.Good.color()
    NewtSettingRow(
        title = title,
        modifier = modifier.clearAndSetSemantics { contentDescription = "$title, $value. $reason" },
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .drawBehind {
                        val w = 2.5.dp.toPx()
                        drawRoundRect(
                            color = good,
                            topLeft = Offset(w / 2, w / 2),
                            size = size.copy(width = size.width - w, height = size.height - w),
                            cornerRadius = CornerRadius(12.dp.toPx()),
                            style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = good, modifier = Modifier.size(18.dp))
                Text(value, color = good, fontFamily = NewtFonts.Mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
    )
}

/** "ⓘ What's this?": longer help that opens in place, so rows keep to one line. */
@Composable
fun NewtInfo(details: String, modifier: Modifier = Modifier, label: String = "What's this?") {
    var open by rememberSaveable { mutableStateOf(false) }
    Column(modifier.padding(horizontal = 14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button) { open = !open }
                .semantics { stateDescription = if (open) "Open" else "Closed" },
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (open) {
            Text(details, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

/** "TODAY ————": a heading in capitals with a rule after it, for lists split by day or group. */
@Composable
fun NewtRuleHeading(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 6.dp)
            .clearAndSetSemantics {
                heading()
                contentDescription = text
            },
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = NewtFonts.Mono,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.1.em,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.outlineVariant))
    }
}
