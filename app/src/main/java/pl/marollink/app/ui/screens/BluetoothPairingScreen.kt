package pl.marollink.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.marollink.app.connectivity.DiscoveredBluetoothDevice
import pl.marollink.app.ui.components.CarCard
import pl.marollink.app.ui.components.CarPrimaryButton
import pl.marollink.app.ui.components.CarSecondaryButton
import pl.marollink.app.ui.components.StatusChip
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.ui.theme.rememberCarClickFeedback

@Composable
fun BluetoothPairingScreen(
    pairedDevices: List<DiscoveredBluetoothDevice>,
    nearbyDevices: List<DiscoveredBluetoothDevice>,
    isScanning: Boolean,
    statusMessage: String?,
    uiSoundsEnabled: Boolean,
    onSelectDevice: (DiscoveredBluetoothDevice) -> Unit,
    onSearchAgain: () -> Unit,
    onOpenSystemBluetooth: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarPalette.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CarSecondaryButton(
                    text = "Powrót",
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBack,
                    uiSoundsEnabled = uiSoundsEnabled
                )
                Text(
                    text = "Połącz telefon (Bluetooth)",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CarSecondaryButton(
                    text = "Pokaż wszystkie urządzenia",
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenSystemBluetooth,
                    uiSoundsEnabled = uiSoundsEnabled
                )
                CarPrimaryButton(
                    text = if (isScanning) "Szukam…" else "Szukaj ponownie",
                    icon = Icons.Rounded.Refresh,
                    onClick = onSearchAgain,
                    enabled = !isScanning,
                    uiSoundsEnabled = uiSoundsEnabled
                )
            }
        }

        if (!statusMessage.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CarPalette.Warning.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, CarPalette.Warning)
            ) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CarPalette.Warning,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BluetoothDeviceListColumn(
                title = "Sparowane telefony (${pairedDevices.size})",
                emptyHint = "Brak sparowanych urządzeń w pamięci Androida.",
                devices = pairedDevices,
                isScanning = false,
                uiSoundsEnabled = uiSoundsEnabled,
                onSelect = onSelectDevice,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            BluetoothDeviceListColumn(
                title = "Wykryte w pobliżu (${nearbyDevices.size})",
                emptyHint = "Kliknij „Szukaj ponownie” z włączonym Bluetooth w telefonie.",
                devices = nearbyDevices,
                isScanning = isScanning,
                uiSoundsEnabled = uiSoundsEnabled,
                onSelect = onSelectDevice,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun BluetoothDeviceListColumn(
    title: String,
    emptyHint: String,
    devices: List<DiscoveredBluetoothDevice>,
    isScanning: Boolean,
    uiSoundsEnabled: Boolean,
    onSelect: (DiscoveredBluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val playClick = rememberCarClickFeedback(uiSoundsEnabled)
    CarCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (devices.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bluetooth,
                    contentDescription = null,
                    tint = CarPalette.TextSecondary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = emptyHint,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CarPalette.TextSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(devices, key = { it.address }) { dev ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                playClick()
                                onSelect(dev)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CarPalette.SurfaceElevated,
                        border = BorderStroke(1.dp, CarPalette.Border)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhoneAndroid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = dev.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = dev.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CarPalette.TextSecondary
                                    )
                                }
                            }
                            StatusChip(
                                text = if (dev.isBonded) "Sparowany" else "Połącz",
                                color = if (dev.isBonded) CarPalette.Success else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
