package pl.marollink.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import pl.marollink.app.connectivity.AttachedUsbInfo
import pl.marollink.app.connectivity.ProjectionSessionState
import pl.marollink.app.data.AppSettings
import pl.marollink.app.data.DeviceTransportType
import pl.marollink.app.data.KnownDevice
import pl.marollink.app.ui.components.CalibrationGridOverlay
import pl.marollink.app.ui.components.CarCard
import pl.marollink.app.ui.components.CarPrimaryButton
import pl.marollink.app.ui.components.CarSecondaryButton
import pl.marollink.app.ui.components.FloatingControlsBar
import pl.marollink.app.ui.components.FpsHudBadge
import pl.marollink.app.ui.components.StatusChip
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.ui.theme.rememberCarClickFeedback

@Composable
fun ProjectionScreen(
    settings: AppSettings,
    sessionState: ProjectionSessionState,
    knownDevices: List<KnownDevice>,
    conflictUsbCandidate: AttachedUsbInfo?,
    onCancelOrDisconnect: () -> Unit,
    onReconnect: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwitchToDevice: (KnownDevice) -> Unit,
    onForgetDevice: (KnownDevice) -> Unit,
    onResolveConflictUseCable: () -> Unit,
    onResolveConflictKeepWireless: () -> Unit,
    onFourFingerTripleTap: () -> Unit
) {
    var showStatsDialog by remember { mutableStateOf(false) }
    var showPhoneSwitcherDialog by remember { mutableStateOf(false) }
    var markerToastVisible by remember { mutableStateOf(false) }

    var fourFingerTapCount by remember { mutableIntStateOf(0) }
    var lastFourFingerTapMs by remember { mutableLongStateOf(0L) }
    var fourFingerCurrentlyDown by remember { mutableStateOf(false) }

    if (markerToastVisible) {
        LaunchedEffect(markerToastVisible) {
            delay(2_200L)
            markerToastVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarPalette.Background)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 4 && !fourFingerCurrentlyDown) {
                            fourFingerCurrentlyDown = true
                            val now = System.currentTimeMillis()
                            fourFingerTapCount = if (now - lastFourFingerTapMs <= 1_000L) {
                                fourFingerTapCount + 1
                            } else {
                                1
                            }
                            lastFourFingerTapMs = now
                            if (fourFingerTapCount >= 3) {
                                fourFingerTapCount = 0
                                markerToastVisible = true
                                onFourFingerTripleTap()
                            }
                        } else if (pressedCount < 2) {
                            fourFingerCurrentlyDown = false
                        }
                    }
                }
            }
    ) {
        // Respect user's calibrated safe area margins
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = settings.safeAreaTopDp.dp,
                    bottom = settings.safeAreaBottomDp.dp,
                    start = settings.safeAreaLeftDp.dp,
                    end = settings.safeAreaRightDp.dp
                )
        ) {
            when (sessionState) {
                is ProjectionSessionState.Connecting -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CarCard(modifier = Modifier.widthIn(min = 480.dp, max = 660.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                                Column {
                                    Text(
                                        text = "Łączenie z ${sessionState.device.name}",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = sessionState.stagePl,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = CarPalette.TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            LinearProgressIndicator(
                                progress = {
                                    sessionState.stepIndex.toFloat() / sessionState.totalSteps.coerceAtLeast(1)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                CarSecondaryButton(
                                    text = "Anuluj",
                                    icon = Icons.Rounded.Close,
                                    onClick = onCancelOrDisconnect,
                                    uiSoundsEnabled = settings.uiSoundsEnabled
                                )
                            }
                        }
                    }
                }

                is ProjectionSessionState.Active -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CarCard(
                            modifier = Modifier.widthIn(max = 820.dp).fillMaxWidth()
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
                                    Icon(
                                        imageVector = if (sessionState.device.transportType == DeviceTransportType.BLUETOOTH) {
                                            Icons.Rounded.Bluetooth
                                        } else {
                                            Icons.Rounded.Usb
                                        },
                                        contentDescription = null,
                                        tint = CarPalette.Success,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Sesja aktywna: ${sessionState.device.name}",
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Text(
                                            text = "${sessionState.device.protocol.labelPl} • ${sessionState.device.transportType.labelPl}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = CarPalette.Success
                                        )
                                    }
                                }
                                StatusChip(text = "ONLINE", color = CarPalette.Success)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = CarPalette.Background,
                                border = BorderStroke(1.dp, CarPalette.Border)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = sessionState.transportNotePl,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = CarPalette.TextPrimary
                                    )
                                    Text(
                                        text = "Wskazówka diagnostyczna: potrójne stuknięcie czterema palcami w dowolnym miejscu zapisuje znacznik błędu w lokalnym dzienniku.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CarPalette.TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    if (settings.showFpsCounter) {
                        FpsHudBadge(
                            fps = sessionState.fps,
                            decodeLatencyMs = sessionState.decodeLatencyMs,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        )
                    }

                    if (settings.floatingControlsEnabled) {
                        FloatingControlsBar(
                            uiSoundsEnabled = settings.uiSoundsEnabled,
                            onOpenSettings = onOpenSettings,
                            onOpenStats = { showStatsDialog = true },
                            onSwitchPhone = { showPhoneSwitcherDialog = true },
                            onReconnect = onReconnect,
                            onDisconnect = onCancelOrDisconnect,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        )
                    }
                }

                is ProjectionSessionState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CarCard(modifier = Modifier.widthIn(max = 640.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = CarPalette.Danger,
                                    modifier = Modifier.size(34.dp)
                                )
                                Column {
                                    Text("Przerwano połączenie", style = MaterialTheme.typography.headlineSmall)
                                    Text(
                                        text = sessionState.reasonPl,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = CarPalette.TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                            ) {
                                CarSecondaryButton(
                                    text = "Powrót",
                                    onClick = onCancelOrDisconnect,
                                    uiSoundsEnabled = settings.uiSoundsEnabled
                                )
                                CarPrimaryButton(
                                    text = "Połącz ponownie",
                                    icon = Icons.Rounded.Refresh,
                                    onClick = onReconnect,
                                    uiSoundsEnabled = settings.uiSoundsEnabled
                                )
                            }
                        }
                    }
                }

                ProjectionSessionState.Disconnected -> Unit
            }
        }

        if (settings.panelCalibrationGridEnabled) {
            CalibrationGridOverlay()
        }

        if (markerToastVisible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Zapisano znacznik diagnostyczny w dzienniku (4 palce x3)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }

        // USB vs Wireless Conflict Dialog (per PRD Step 4.3)
        if (conflictUsbCandidate != null) {
            Dialog(onDismissRequest = onResolveConflictKeepWireless) {
                CarCard(modifier = Modifier.widthIn(min = 440.dp, max = 600.dp)) {
                    Text(
                        text = "Wykryto połączenie USB podczas sesji bezprzewodowej",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Podłączono: ${conflictUsbCandidate.displayTitle}. Jak chcesz kontynuować?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CarPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CarSecondaryButton(
                            text = "Kontynuuj bezprzewodowo",
                            onClick = onResolveConflictKeepWireless,
                            uiSoundsEnabled = settings.uiSoundsEnabled,
                            modifier = Modifier.weight(1f)
                        )
                        CarPrimaryButton(
                            text = "Użyj kabla",
                            icon = Icons.Rounded.Usb,
                            onClick = onResolveConflictUseCable,
                            uiSoundsEnabled = settings.uiSoundsEnabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick Stats Dialog
        if (showStatsDialog && sessionState is ProjectionSessionState.Active) {
            Dialog(onDismissRequest = { showStatsDialog = false }) {
                CarCard(modifier = Modifier.widthIn(min = 420.dp, max = 560.dp)) {
                    Text("Statystyki sesji", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Urządzenie: ${sessionState.device.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Transport: ${sessionState.device.transportType.labelPl}", style = MaterialTheme.typography.bodyLarge)
                    Text("Protokół: ${sessionState.device.protocol.labelPl}", style = MaterialTheme.typography.bodyLarge)
                    Text("Klatki (FPS): ${sessionState.fps}", style = MaterialTheme.typography.bodyLarge)
                    Text("Opóźnienie dekodera: ${sessionState.decodeLatencyMs} ms", style = MaterialTheme.typography.bodyLarge)
                    Text("Wzmocnienie głośności: +${settings.volumeBoostDb} dB", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        CarPrimaryButton(
                            text = "Zamknij",
                            onClick = { showStatsDialog = false },
                            uiSoundsEnabled = settings.uiSoundsEnabled
                        )
                    }
                }
            }
        }

        // "Wybierz telefon" Switcher & Forget Dialog (per PRD Step 4.4)
        if (showPhoneSwitcherDialog) {
            val playClick = rememberCarClickFeedback(settings.uiSoundsEnabled)
            Dialog(onDismissRequest = { showPhoneSwitcherDialog = false }) {
                CarCard(modifier = Modifier.widthIn(min = 480.dp, max = 660.dp)) {
                    Text("Wybierz telefon", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(knownDevices, key = { it.id }) { dev ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        playClick()
                                        showPhoneSwitcherDialog = false
                                        onSwitchToDevice(dev)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = CarPalette.SurfaceElevated,
                                border = BorderStroke(1.dp, CarPalette.Border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
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
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(dev.name, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "${dev.protocol.labelPl} • ${dev.transportType.labelPl}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = CarPalette.TextSecondary
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            playClick()
                                            onForgetDevice(dev)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Zapomnij",
                                            tint = CarPalette.Danger
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        CarSecondaryButton(
                            text = "Zamknij",
                            onClick = { showPhoneSwitcherDialog = false },
                            uiSoundsEnabled = settings.uiSoundsEnabled
                        )
                    }
                }
            }
        }
    }
}
