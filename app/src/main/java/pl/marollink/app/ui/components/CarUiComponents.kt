package pl.marollink.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.ui.theme.rememberCarClickFeedback
import kotlin.math.roundToInt

@Composable
fun CarCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CarPalette.Surface,
    borderColor: Color = CarPalette.Border,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun CarPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    uiSoundsEnabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    val playClick = rememberCarClickFeedback(uiSoundsEnabled)
    Button(
        onClick = {
            playClick()
            onClick()
        },
        enabled = enabled,
        modifier = modifier.heightIn(min = 58.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CarSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    uiSoundsEnabled: Boolean = true
) {
    val playClick = rememberCarClickFeedback(uiSoundsEnabled)
    OutlinedButton(
        onClick = {
            playClick()
            onClick()
        },
        enabled = enabled,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, CarPalette.Border),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CarPalette.SurfaceElevated,
            contentColor = CarPalette.TextPrimary
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CarSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    uiSoundsEnabled: Boolean = true
) {
    val playClick = rememberCarClickFeedback(uiSoundsEnabled)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                playClick()
                onCheckedChange(!checked)
            }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = CarPalette.TextPrimary)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CarPalette.TextSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { newValue ->
                playClick()
                onCheckedChange(newValue)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun CarSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    steps: Int = 0,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = CarPalette.TextPrimary)
                if (!subtitle.isNullOrBlank()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = CarPalette.TextSecondary)
                }
            }
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun <T> CarOptionSelector(
    title: String,
    options: List<T>,
    selected: T,
    labelMapper: (T) -> String,
    onSelect: (T) -> Unit,
    uiSoundsEnabled: Boolean = true,
    subtitle: String? = null
) {
    val playClick = rememberCarClickFeedback(uiSoundsEnabled)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = CarPalette.TextPrimary)
        if (!subtitle.isNullOrBlank()) {
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = CarPalette.TextSecondary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { item ->
                val isSelected = item == selected
                val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else CarPalette.SurfaceElevated
                val border = if (isSelected) MaterialTheme.colorScheme.primary else CarPalette.Border
                val textColor = if (isSelected) MaterialTheme.colorScheme.primary else CarPalette.TextPrimary
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .border(1.5.dp, border, RoundedCornerShape(12.dp))
                        .clickable {
                            playClick()
                            onSelect(item)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelMapper(item),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

/**
 * Draws a numbered 20dp grid across the screen for safe-area and cluster visibility calibration (per PRD).
 */
@Composable
fun CalibrationGridOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepPx = 20.dp.toPx()
            val gridColor = Color.Cyan.copy(alpha = 0.45f)
            var x = 0f
            while (x <= size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
                x += stepPx
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
                y += stepPx
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Siatka kalibracyjna co 20 dp",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Cyan
            )
            Text(
                text = "Krok = ${(20.dp.value).roundToInt()} dp",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}
