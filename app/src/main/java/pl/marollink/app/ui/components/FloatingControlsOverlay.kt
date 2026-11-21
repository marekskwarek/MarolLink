package pl.marollink.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.ui.theme.rememberCarClickFeedback

@Composable
fun FpsHudBadge(
    fps: Int,
    decodeLatencyMs: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
    ) {
        Text(
            text = "FPS: $fps • ${decodeLatencyMs} ms",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FloatingControlsBar(
    uiSoundsEnabled: Boolean,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onSwitchPhone: () -> Unit,
    onReconnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CarPalette.Surface.copy(alpha = 0.94f),
        border = BorderStroke(1.5.dp, CarPalette.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingPillAction(Icons.Rounded.Settings, "Ustawienia", uiSoundsEnabled, onOpenSettings)
            FloatingPillAction(Icons.Rounded.Speed, "Statystyki", uiSoundsEnabled, onOpenStats)
            FloatingPillAction(Icons.Rounded.SwapHoriz, "Zmień telefon", uiSoundsEnabled, onSwitchPhone)
            FloatingPillAction(Icons.Rounded.Refresh, "Połącz ponownie", uiSoundsEnabled, onReconnect)
            FloatingPillAction(
                icon = Icons.Rounded.Close,
                label = "Rozłącz",
                uiSoundsEnabled = uiSoundsEnabled,
                onClick = onDisconnect,
                tint = CarPalette.Danger
            )
        }
    }
}

@Composable
private fun FloatingPillAction(
    icon: ImageVector,
    label: String,
    uiSoundsEnabled: Boolean,
    onClick: () -> Unit,
    tint: Color = CarPalette.TextPrimary
) {
    val playClick = rememberCarClickFeedback(uiSoundsEnabled)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CarPalette.SurfaceElevated)
            .clickable {
                playClick()
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = tint)
    }
}
