package com.madstudy.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    gradient: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val p = LocalPalette.current
    if (p.pixel) {
        androidx.compose.material3.Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (gradient) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) { Column(Modifier.padding(20.dp), content = content) }
        return
    }
    val shape = MaterialTheme.shapes.medium
    val brush = if (gradient) {
        Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.surface, p.scheme.primaryContainer),
            start = Offset(0f, 0f),
            end = Offset(600f, 700f)
        )
    } else {
        SolidColor(MaterialTheme.colorScheme.surface)
    }
    Box(
        modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, clip = false)
            .clip(shape)
            .background(brush)
            .border(1.dp, p.line, shape)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun Chip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            style = MaterialTheme.typography.copy().labelSmall.copy(fontSize = 10.5.sp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun IconBtn(icon: ImageVector, desc: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.pixel) {
        androidx.compose.material3.FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(icon, desc, modifier = Modifier.size(24.dp))
        }
        return
    }
    Box(
        Modifier
            .size(38.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, p.line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SectionTitle(text: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 9.dp, start = 2.dp, end = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.1).sp)
        if (action != null && onAction != null) {
            Text(
                action,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, p.line, shape)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BigStat(value: String, label: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(if (p.pixel) 28.dp else 20.dp)
    Column(
        modifier
            .shadow(if (p.pixel) 0.dp else 2.dp, shape, clip = false)
            .clip(shape)
            .background(if (p.pixel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
            .border(if (p.pixel) 0.dp else 1.dp, p.line, shape)
            .padding(15.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    if (p.pixel) {
        androidx.compose.material3.Button(onClick = onClick, enabled = enabled,
            modifier = modifier.fillMaxWidth().height(56.dp), shape = CircleShape) {
            if (icon != null) { Icon(icon, null, Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)) }
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
        return
    }
    val shape = RoundedCornerShape(if (p.pill) 24.dp else 18.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(14.dp, shape, clip = false, ambientColor = p.glow, spotColor = p.glow)
            .clip(shape)
            .background(
                if (enabled) Brush.linearGradient(listOf(p.gradA, p.gradB))
                else SolidColor(p.line)
            )
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
        }
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.pixel) {
        androidx.compose.material3.FilledTonalButton(onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = CircleShape) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
        return
    }
    val shape = RoundedCornerShape(if (p.pill) 22.dp else 16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, p.line, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PageHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            val p = LocalPalette.current
            Box(
                Modifier
                    .size(38.dp)
                    .shadow(2.dp, RoundedCornerShape(13.dp), clip = false)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, p.line, RoundedCornerShape(13.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", modifier = Modifier.size(17.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CheckCircle(done: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.pixel) {
        androidx.compose.material3.Checkbox(checked = done, onCheckedChange = { onClick() }, modifier = Modifier.size(40.dp))
        return
    }
    val shape = RoundedCornerShape(7.dp)
    Box(
        Modifier.size(24.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(22.dp)
                .background(if (done) MaterialTheme.colorScheme.primary else Color.Transparent, shape)
                .border(2.dp, if (done) MaterialTheme.colorScheme.primary else p.line, shape),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PriorityDot(prio: String) {
    val color = when (prio) {
        "high" -> Color(0xFFD98B7A)
        "low" -> Color(0xFF8FB39A)
        else -> Color(0xFFD9BC7A)
    }
    Box(Modifier.size(7.dp).background(color, CircleShape))
}

@Composable
fun ToggleSwitch(checked: Boolean, onChange: () -> Unit) {
    val p = LocalPalette.current
    if (p.pixel) {
        androidx.compose.material3.Switch(checked = checked, onCheckedChange = { onChange() })
        return
    }
    Box(
        Modifier
            .size(width = 46.dp, height = 27.dp)
            .clip(CircleShape)
            .background(if (checked) MaterialTheme.colorScheme.primary else p.line)
            .clickable(onClick = onChange)
    ) {
        Box(
            Modifier
                .offset(x = if (checked) 22.dp else 3.dp, y = 3.dp)
                .size(21.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun GroupLabel(text: String) {
    Text(
        text.uppercase(),
        Modifier.padding(top = 16.dp, bottom = 8.dp, start = 2.dp),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(LocalPalette.current.line))
}

@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.padding(vertical = 10.dp, horizontal = 2.dp),
        fontSize = 12.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun FilterPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.pixel) {
        androidx.compose.material3.FilterChip(selected = selected, onClick = onClick,
            label = { Text(text) }, modifier = modifier, shape = RoundedCornerShape(12.dp))
        return
    }
    val shape = CircleShape
    Text(
        text,
        modifier
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else p.line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun FieldLabel(text: String) {
    Text(
        text,
        Modifier.padding(top = 14.dp, bottom = 6.dp),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Small pill action (e.g. "Selesai") used on task rows. */
@Composable
fun ActionPill(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(13.dp)
        )
        Text(text, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val pixel = LocalPalette.current.pixel
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (pixel) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        dragHandle = { if (pixel) androidx.compose.material3.BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = if (pixel) 32.dp else 26.dp, topEnd = if (pixel) 32.dp else 26.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconBtn(Icons.Rounded.Close, "Tutup", onDismiss)
            }
            Spacer(Modifier.height(6.dp))
            content()
            Spacer(Modifier.height(12.dp))
        }
    }
}
