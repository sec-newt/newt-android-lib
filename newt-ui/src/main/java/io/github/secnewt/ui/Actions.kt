package io.github.secnewt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** An outlined button with words: 58dp tall, 64dp when [large]. */
@Composable
fun NewtButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    large: Boolean = false,
) = ButtonBody(text, onClick, modifier, icon, large, fill = null, border = MaterialTheme.colorScheme.primary, borderWidth = 2.5f, content = MaterialTheme.colorScheme.onSurface)

/**
 * The main button on a screen. [tone]: Info = the blue-to-purple fill, Good = green fill (go,
 * call, directions), Danger = red outline (never a red fill), Neutral = like [NewtButton].
 */
@Composable
fun NewtPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: NewtTone = NewtTone.Info,
    large: Boolean = false,
) {
    when (tone) {
        NewtTone.Info -> ButtonBody(text, onClick, modifier, icon, large, fill = null, gradient = true, content = onNewtGradient())
        NewtTone.Good -> ButtonBody(text, onClick, modifier, icon, large, fill = NewtTone.Good.color(), content = onNewtGradient())
        NewtTone.Danger -> ButtonBody(
            text, onClick, modifier, icon, large, fill = null,
            border = NewtTone.Danger.color(), borderWidth = 3f, content = NewtTone.Danger.color(),
        )
        NewtTone.Neutral -> NewtButton(text, onClick, modifier, icon, large)
    }
}

@Composable
private fun ButtonBody(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    icon: ImageVector?,
    large: Boolean,
    fill: Color?,
    content: Color,
    gradient: Boolean = false,
    border: Color? = null,
    borderWidth: Float = 0f,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .heightIn(min = if (large) 64.dp else 58.dp)
            .clip(shape)
            .then(
                when {
                    gradient -> Modifier.background(newtGradient())
                    fill != null -> Modifier.background(fill)
                    else -> Modifier
                },
            )
            .then(if (border != null) Modifier.border(borderWidth.dp, border, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        Text(text, color = content, fontWeight = FontWeight.Bold, fontSize = if (large) 22.sp else 19.sp, textAlign = TextAlign.Center)
    }
}

/** One action in a [NewtActionGrid]. Give a [hint] only where it matters ("Search still finds it"). */
data class NewtAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val hint: String? = null,
)

/** Actions as a two-column grid of big tiles, for menus that open from the bottom. */
@Composable
fun NewtActionGrid(actions: List<NewtAction>, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { action ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .heightIn(min = 66.dp)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                            .clickable(role = Role.Button, onClick = action.onClick)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (action.icon != null) {
                                Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                            }
                            Text(action.label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        if (action.hint != null) {
                            Text(action.hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

enum class NewtStepState { Done, Now, ToDo }

/** One step of a first-run list. [action] (a button) shows only while the step is [NewtStepState.Now]. */
data class NewtStep(
    val title: String,
    val state: NewtStepState,
    val hint: String? = null,
    val action: (@Composable () -> Unit)? = null,
)

/** Numbered first-run steps: done ones get a green check, the current one is lit and has its button. */
@Composable
fun NewtSteps(steps: List<NewtStep>, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEachIndexed { index, step ->
            val number = index + 1
            val now = step.state == NewtStepState.Now
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(2.dp, if (now) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                StepCircle(number, step.state)
                Column(Modifier.weight(1f)) {
                    val state = when (step.state) {
                        NewtStepState.Done -> "done"
                        NewtStepState.Now -> "current"
                        NewtStepState.ToDo -> "not yet"
                    }
                    Text(
                        step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "Step $number of ${steps.size}, ${step.title}, $state"
                        },
                    )
                    if (step.hint != null) {
                        Text(
                            step.hint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    if (now && step.action != null) {
                        Box(Modifier.padding(top = 10.dp)) { step.action.invoke() }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCircle(number: Int, state: NewtStepState) {
    // The title already says "Step n of m", so the circle is decoration for screen readers. It
    // grows with the phone's font size (up to 1.5×) so the number never crowds it.
    val grow = LocalDensity.current.fontScale.coerceIn(1f, 1.5f)
    val base = Modifier.size(36.dp * grow).clip(CircleShape).clearAndSetSemantics { }
    Box(
        contentAlignment = Alignment.Center,
        modifier = when (state) {
            NewtStepState.Done -> base.background(NewtTone.Good.color())
            NewtStepState.Now -> base.background(newtGradient())
            NewtStepState.ToDo -> base.border(2.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
        },
    ) {
        if (state == NewtStepState.Done) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = onNewtGradient(), modifier = Modifier.size(22.dp))
        } else {
            Text(
                "$number",
                fontFamily = NewtFonts.Mono,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (state == NewtStepState.Now) onNewtGradient() else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
