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
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.marollink.app.BuildConfig
import pl.marollink.app.connectivity.AttachedUsbInfo
import pl.marollink.app.data.AppSettings
import pl.marollink.app.data.DeviceTransportType
import pl.marollink.app.data.KnownDevice
import pl.marollink.app.ui.components.CarCard
import pl.marollink.app.ui.components.CarPrimaryButton
import pl.marollink.app.ui.components.CarSecondaryButton
import pl.marollink.app.ui.components.StatusChip
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.ui.theme.rememberCarClickFeedback
import pl.marollink.app.updater.VerifiedGitHubRelease

@Composable
fun HomeScreen(
    settings: AppSettings,
    knownDevices: List<KnownDevice>,
    usbDevices: List<AttachedUsbInfo>,
    availableUpdate: VerifiedGitHubRelease?,
    onConnectWireless: () -> Unit,
    onConnectKnownDevice: (KnownDevice) -> Unit,
    onForgetKnownDevice: (KnownDevice) -> Unit,
    onConnectUsbDevice: (AttachedUsbInfo) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUpdateOrCheck: () -> Unit
) {
    val playClick = rememberCarClickFeedback(settings.uiSoundsEnabled)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarPalette.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = "MarolLink",
                    style = MaterialTheme.typography.headlineLarge
                )
                StatusChip(
                    text = "v${BuildConfig.VERSION_NAME}",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CarSecondaryButton(
                    text = if (availableUpdate != null) {
                        "Aktualizacja ${availableUpdate.tagName}"
                    } else {
                        "Sprawdź aktualizacje"
                    },
                    icon = Icons.Rounded.SystemUpdate,
                    onClick = onOpenUpdateOrCheck,
                    uiSoundsEnabled = settings.uiSoundsEnabled
                )
                CarSecondaryButton(
                    text = "Ustawienia",
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenSettings,
                    uiSoundsEnabled = settings.uiSoundsEnabled
                )
            }
        }

        // Main landscape split workspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Left panel: Primary connection actions & USB detection
            CarCard(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Połącz telefon z samochodem",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Podłącz telefon kablem USB, użyj adaptera USB Carlinkit / AutoKit albo połącz się bezprzewodowo przez Bluetooth i Wi-Fi.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CarPalette.TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                CarPrimaryButton(
                    text = "Połącz telefon bezprzewodowo",
                    icon = Icons.Rounded.Bluetooth,
                    onClick = onConnectWireless,
                    uiSoundsEnabled = settings.uiSoundsEnabled,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Wykryte urządzenia USB (${usbDevices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = CarPalette.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (usbDevices.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = CarPalette.Background,
                        border = BorderStroke(1.dp, CarPalette.Border)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Usb,
                                contentDescription = null,
                                tint = CarPalette.TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Brak podłączonego telefonu ani adaptera na porcie USB.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CarPalette.TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(usbDevices, key = { it.deviceName }) { usb ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        playClick()
                                        onConnectUsbDevice(usb)
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = CarPalette.SurfaceElevated,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Usb,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column {
                                            Text(
                                                text = usb.displayTitle,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = if (usb.hasPermission) {
                                                    "Kliknij, aby uruchomić sesję USB"
                                                } else {
                                                    "Wymaga zgody USB — kliknij, aby zezwolić"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = CarPalette.TextSecondary
                                            )
                                        }
                                    }
                                    StatusChip(
                                        text = if (usb.isAutoKitAdapter) "AutoKit" else "USB",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right panel: Known / recently connected devices
            CarCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Ostatnie połączenia i urządzenia",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Wybierz zapisany telefon, aby natychmiast wznowić połączenie.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CarPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (knownDevices.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhoneAndroid,
                            contentDescription = null,
                            tint = CarPalette.TextSecondary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Lista urządzeń jest jeszcze pusta.",
                            style = MaterialTheme.typography.titleMedium,
                            color = CarPalette.TextSecondary
                        )
                        Text(
                            text = "Po pierwszym połączeniu telefon zapisze się tutaj automatycznie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CarPalette.TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(knownDevices, key = { it.id }) { device ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        playClick()
                                        onConnectKnownDevice(device)
                                    },
                                shape = RoundedCornerShape(14.dp),
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
                                            imageVector = when (device.transportType) {
                                                DeviceTransportType.BLUETOOTH -> Icons.Rounded.Bluetooth
                                                else -> Icons.Rounded.Usb
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = device.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${device.protocol.labelPl} • ${device.transportType.labelPl}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = CarPalette.TextSecondary
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            playClick()
                                            onForgetKnownDevice(device)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Zapomnij",
                                            tint = CarPalette.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
