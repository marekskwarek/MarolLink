package pl.marollink.app.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pl.marollink.app.BuildConfig
import pl.marollink.app.connectivity.PermissionStatusItem
import pl.marollink.app.connectivity.WirelessDiagnosticCheck
import pl.marollink.app.data.AppLanguage
import pl.marollink.app.data.AppSettings
import pl.marollink.app.data.DiagnosticEntry
import pl.marollink.app.data.MicSource
import pl.marollink.app.data.PanelPriority
import pl.marollink.app.data.SteeringAction
import pl.marollink.app.data.VideoResolutionPreset
import pl.marollink.app.data.WirelessTransportMode
import pl.marollink.app.ui.components.CarCard
import pl.marollink.app.ui.components.CarOptionSelector
import pl.marollink.app.ui.components.CarPrimaryButton
import pl.marollink.app.ui.components.CarSecondaryButton
import pl.marollink.app.ui.components.CarSliderRow
import pl.marollink.app.ui.components.CarSwitchRow
import pl.marollink.app.ui.components.StatusChip
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.ui.theme.rememberCarClickFeedback
import pl.marollink.app.updater.VerifiedGitHubRelease
import kotlin.math.roundToInt

enum class SettingsTab(val labelPl: String, val icon: ImageVector) {
    GENERAL("Ogólne", Icons.Rounded.Tune),
    CARPLAY("CarPlay", Icons.Rounded.PhoneAndroid),
    ANDROID_AUTO("Android Auto", Icons.Rounded.DirectionsCar),
    AUTOKIT("AutoKit", Icons.Rounded.Usb),
    PANEL("Panel", Icons.Rounded.AspectRatio),
    ABOUT("Informacje", Icons.Rounded.Info)
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    permissions: List<PermissionStatusItem>,
    diagnosticEntries: List<DiagnosticEntry>,
    wirelessChecks: List<WirelessDiagnosticCheck>,
    availableUpdate: VerifiedGitHubRelease?,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onClearDevices: () -> Unit,
    onResetSettings: () -> Unit,
    onRequestMissingPermissions: () -> Unit,
    onOpenAppSystemSettings: () -> Unit,
    onPickRecordingFolder: () -> Unit,
    onRunWirelessDiagnostics: () -> Unit,
    onShareLogs: () -> Unit,
    onClearLogs: () -> Unit,
    onOpenSafeAreaEditor: () -> Unit,
    onCheckUpdates: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(SettingsTab.GENERAL) }
    var showSteeringModal by remember { mutableStateOf(false) }
    var showAdvancedDiagModal by remember { mutableStateOf(false) }
    var showWirelessDiagModal by remember { mutableStateOf(false) }
    var showResetConfirmModal by remember { mutableStateOf(false) }

    val playClick = rememberCarClickFeedback(settings.uiSoundsEnabled)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(CarPalette.Background)
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Left navigation rail
        CarCard(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
        ) {
            CarSecondaryButton(
                text = "Powrót",
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                onClick = onBack,
                uiSoundsEnabled = settings.uiSoundsEnabled,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else CarPalette.SurfaceElevated
                    val border = if (isSelected) MaterialTheme.colorScheme.primary else CarPalette.Border
                    val tint = if (isSelected) MaterialTheme.colorScheme.primary else CarPalette.TextPrimary
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                playClick()
                                selectedTab = tab
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = bg,
                        border = BorderStroke(1.5.dp, border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = tab.icon, contentDescription = null, tint = tint)
                            Text(
                                text = tab.labelPl,
                                style = MaterialTheme.typography.titleMedium,
                                color = tint
                            )
                        }
                    }
                }
            }
        }

        // Right detail pane
        CarCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    SettingsTab.GENERAL -> GeneralSettingsTab(
                        settings = settings,
                        permissions = permissions,
                        onUpdateSettings = onUpdateSettings,
                        onClearDevices = onClearDevices,
                        onOpenSafeAreaEditor = onOpenSafeAreaEditor,
                        onOpenSteeringMapper = { showSteeringModal = true },
                        onRequestMissingPermissions = onRequestMissingPermissions,
                        onOpenAppSystemSettings = onOpenAppSystemSettings,
                        onPickRecordingFolder = onPickRecordingFolder,
                        onOpenAdvancedDiagnostics = { showAdvancedDiagModal = true },
                        onRequestReset = { showResetConfirmModal = true }
                    )

                    SettingsTab.CARPLAY -> CarPlaySettingsTab(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings
                    )

                    SettingsTab.ANDROID_AUTO -> AndroidAutoSettingsTab(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings,
                        onRunWirelessDiagnostics = {
                            onRunWirelessDiagnostics()
                            showWirelessDiagModal = true
                        }
                    )

                    SettingsTab.AUTOKIT -> AutoKitSettingsTab(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings
                    )

                    SettingsTab.PANEL -> PanelSettingsTab(
                        settings = settings,
                        onUpdateSettings = onUpdateSettings
                    )

                    SettingsTab.ABOUT -> AboutSettingsTab(
                        settings = settings,
                        availableUpdate = availableUpdate,
                        onCheckUpdates = onCheckUpdates,
                        onOpenLogs = { showAdvancedDiagModal = true }
                    )
                }
            }
        }
    }

    if (showSteeringModal) {
        SteeringWheelMapperDialog(
            settings = settings,
            onAssignKey = { action, keyCode ->
                onUpdateSettings {
                    val updated = it.steeringMappings.toMutableMap()
                    updated[action] = keyCode
                    it.copy(steeringMappings = updated)
                }
            },
            onResetDefaults = {
                onUpdateSettings { it.copy(steeringMappings = AppSettings.defaultSteeringMappings()) }
            },
            onDismiss = { showSteeringModal = false }
        )
    }

    if (showAdvancedDiagModal) {
        AdvancedDiagnosticsDialog(
            settings = settings,
            entries = diagnosticEntries,
            onShareLogs = onShareLogs,
            onClearLogs = onClearLogs,
            onDismiss = { showAdvancedDiagModal = false }
        )
    }

    if (showWirelessDiagModal) {
        WirelessDiagnosticsDialog(
            checks = wirelessChecks,
            uiSoundsEnabled = settings.uiSoundsEnabled,
            onRerun = onRunWirelessDiagnostics,
            onDismiss = { showWirelessDiagModal = false }
        )
    }

    if (showResetConfirmModal) {
        Dialog(onDismissRequest = { showResetConfirmModal = false }) {
            CarCard(modifier = Modifier.widthIn(min = 420.dp, max = 560.dp)) {
                Text("Przywrócić domyślne ustawienia?", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Wszystkie parametry ekranu, dźwięku i projekcji zostaną zresetowane. Zapisana lista telefonów pozostanie nienaruszona.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CarPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    CarSecondaryButton(
                        text = "Anuluj",
                        onClick = { showResetConfirmModal = false },
                        uiSoundsEnabled = settings.uiSoundsEnabled
                    )
                    CarPrimaryButton(
                        text = "Wyczyść i uruchom ponownie",
                        containerColor = CarPalette.Danger,
                        onClick = {
                            showResetConfirmModal = false
                            onResetSettings()
                        },
                        uiSoundsEnabled = settings.uiSoundsEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralSettingsTab(
    settings: AppSettings,
    permissions: List<PermissionStatusItem>,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onClearDevices: () -> Unit,
    onOpenSafeAreaEditor: () -> Unit,
    onOpenSteeringMapper: () -> Unit,
    onRequestMissingPermissions: () -> Unit,
    onOpenAppSystemSettings: () -> Unit,
    onPickRecordingFolder: () -> Unit,
    onOpenAdvancedDiagnostics: () -> Unit,
    onRequestReset: () -> Unit
) {
    val playClick = rememberCarClickFeedback(settings.uiSoundsEnabled)

    SectionHeader("Połączenie")
    CarSwitchRow(
        title = "Automatyczne uruchamianie po podłączeniu USB",
        subtitle = "Startuje sesję natychmiast po wykryciu kabla lub adaptera.",
        checked = settings.autoConnectUsb,
        onCheckedChange = { checked -> onUpdateSettings { it.copy(autoConnectUsb = checked) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSecondaryButton(
        text = "Wyczyść listę zapisanych telefonów",
        icon = Icons.Rounded.DeleteOutline,
        onClick = onClearDevices,
        uiSoundsEnabled = settings.uiSoundsEnabled
    )

    HorizontalDivider(color = CarPalette.Border)
    SectionHeader("Ekran i wygląd")
    CarSwitchRow(
        title = "Tryb pełnoekranowy",
        subtitle = "Ukrywa systemowe paski Androida na ekranie samochodu.",
        checked = settings.fullscreenMode,
        onCheckedChange = { checked -> onUpdateSettings { it.copy(fullscreenMode = checked) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.hsv(settings.accentHue.coerceIn(0f, 360f), 0.72f, 0.96f))
        )
        Box(modifier = Modifier.weight(1f)) {
            CarSliderRow(
                title = "Kolor MarolLink",
                subtitle = "Dopasuj odcień interfejsu do podświetlenia wnętrza auta.",
                valueLabel = "${settings.accentHue.roundToInt()}°",
                value = settings.accentHue,
                valueRange = 0f..360f,
                onValueChange = { hue -> onUpdateSettings { it.copy(accentHue = hue) } }
            )
        }
    }
    CarSwitchRow(
        title = "Dźwięki interfejsu",
        checked = settings.uiSoundsEnabled,
        onCheckedChange = { v -> onUpdateSettings { it.copy(uiSoundsEnabled = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Animacje przejść",
        checked = settings.animationsEnabled,
        onCheckedChange = { v -> onUpdateSettings { it.copy(animationsEnabled = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Pływające przyciski na ekranie projekcji",
        checked = settings.floatingControlsEnabled,
        onCheckedChange = { v -> onUpdateSettings { it.copy(floatingControlsEnabled = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CarSecondaryButton(
            text = "Kalibracja bezpiecznej strefy ekranu",
            icon = Icons.Rounded.AspectRatio,
            onClick = onOpenSafeAreaEditor,
            uiSoundsEnabled = settings.uiSoundsEnabled
        )
    }

    // Language selector grid (all 10 PRD languages in 2 clean rows of 5)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Język interfejsu (preferowany)", style = MaterialTheme.typography.titleMedium)
        AppLanguage.entries.chunked(5).forEach { rowLangs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowLangs.forEach { lang ->
                    val selected = settings.appLanguage == lang
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                playClick()
                                onUpdateSettings { it.copy(appLanguage = lang) }
                            },
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else CarPalette.SurfaceElevated,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else CarPalette.Border)
                    ) {
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }

    HorizontalDivider(color = CarPalette.Border)
    SectionHeader("Dźwięk i kierownica")
    CarSliderRow(
        title = "Wzmocnienie głośności",
        valueLabel = "+${settings.volumeBoostDb} dB",
        value = settings.volumeBoostDb.toFloat(),
        valueRange = 0f..12f,
        steps = 11,
        onValueChange = { v -> onUpdateSettings { it.copy(volumeBoostDb = v.roundToInt()) } }
    )
    CarSecondaryButton(
        text = "Przyciski na kierownicy (przypisywanie klawiszy)",
        icon = Icons.Rounded.KeyboardVoice,
        onClick = onOpenSteeringMapper,
        uiSoundsEnabled = settings.uiSoundsEnabled
    )

    HorizontalDivider(color = CarPalette.Border)
    SectionHeader("Uprawnienia i diagnostyka")
    permissions.forEach { perm ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(perm.titlePl, style = MaterialTheme.typography.titleMedium)
                Text(perm.descriptionPl, style = MaterialTheme.typography.bodyMedium, color = CarPalette.TextSecondary)
            }
            StatusChip(
                text = if (perm.granted) "OK" else "Brak",
                color = if (perm.granted) CarPalette.Success else CarPalette.Warning
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CarPrimaryButton(
            text = "Poproś o brakujące uprawnienia",
            onClick = onRequestMissingPermissions,
            uiSoundsEnabled = settings.uiSoundsEnabled
        )
        CarSecondaryButton(
            text = "Ustawienia Androida",
            onClick = onOpenAppSystemSettings,
            uiSoundsEnabled = settings.uiSoundsEnabled
        )
    }
    CarSwitchRow(
        title = "Licznik FPS i czasu dekodowania na ekranie",
        checked = settings.showFpsCounter,
        onCheckedChange = { v -> onUpdateSettings { it.copy(showFpsCounter = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CarSecondaryButton(
            text = if (settings.callRecordingFolderUri != null) {
                "Folder nagrań WAV (ustawiony)"
            } else {
                "Wybierz folder pendrive USB (nagrania WAV)"
            },
            onClick = onPickRecordingFolder,
            uiSoundsEnabled = settings.uiSoundsEnabled
        )
        CarSecondaryButton(
            text = "Diagnostyka zaawansowana i logi",
            icon = Icons.Rounded.BugReport,
            onClick = onOpenAdvancedDiagnostics,
            uiSoundsEnabled = settings.uiSoundsEnabled
        )
    }

    HorizontalDivider(color = CarPalette.Border)
    SectionHeader("Reset ustawień")
    CarSecondaryButton(
        text = "Wyczyść ustawienia",
        icon = Icons.Rounded.Refresh,
        onClick = onRequestReset,
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
}

@Composable
private fun CarPlaySettingsTab(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    SectionHeader("Ustawienia Apple CarPlay")
    CarOptionSelector(
        title = "Rozdzielczość wideo",
        options = VideoResolutionPreset.entries,
        selected = settings.carPlayResolution,
        labelMapper = { it.label },
        onSelect = { preset -> onUpdateSettings { it.copy(carPlayResolution = preset) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarOptionSelector(
        title = "Płynność obrazu (FPS)",
        options = listOf(30, 60),
        selected = settings.carPlayFps,
        labelMapper = { "$it FPS" },
        onSelect = { fps -> onUpdateSettings { it.copy(carPlayFps = fps) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Kodek H.265 (HEVC)",
        subtitle = "Mniejsze zapotrzebowanie na pasmo Wi-Fi (jeśli wspierane przez adapter/centralę).",
        checked = settings.carPlayHevcEnabled,
        onCheckedChange = { v -> onUpdateSettings { it.copy(carPlayHevcEnabled = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSliderRow(
        title = "Skalowanie interfejsu (DPI)",
        valueLabel = "${settings.carPlayDpi} DPI",
        value = settings.carPlayDpi.toFloat(),
        valueRange = 160f..320f,
        onValueChange = { v -> onUpdateSettings { it.copy(carPlayDpi = v.roundToInt()) } }
    )
    CarSwitchRow(
        title = "Płynne wideo",
        checked = settings.carPlaySmoothVideo,
        onCheckedChange = { v -> onUpdateSettings { it.copy(carPlaySmoothVideo = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Tryb bezstratny ALAC po kablu USB",
        checked = settings.carPlayAlacLossless,
        onCheckedChange = { v -> onUpdateSettings { it.copy(carPlayAlacLossless = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Wyższa jakość głosu nawigacji",
        checked = settings.carPlayHighQualityNavVoice,
        onCheckedChange = { v -> onUpdateSettings { it.copy(carPlayHighQualityNavVoice = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Main Buffered Audio",
        checked = settings.carPlayMainBufferedAudio,
        onCheckedChange = { v -> onUpdateSettings { it.copy(carPlayMainBufferedAudio = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Kieruj dźwięk przez Bluetooth samochodu",
        checked = settings.carPlayAudioViaCarBt,
        onCheckedChange = { v -> onUpdateSettings { it.copy(carPlayAudioViaCarBt = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Eksperymentalnie rozłącz BT auta przy bezprzewodowym CarPlay",
        checked = settings.carPlayDisconnectCarBtOnWireless,
        onCheckedChange = { v -> onUpdateSettings { it.copy(carPlayDisconnectCarBtOnWireless = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSliderRow(
        title = "Bufor audio (kabel USB)",
        valueLabel = "${settings.carPlayAudioBufferCableMs} ms",
        value = settings.carPlayAudioBufferCableMs.toFloat(),
        valueRange = 20f..300f,
        onValueChange = { v -> onUpdateSettings { it.copy(carPlayAudioBufferCableMs = v.roundToInt()) } }
    )
    CarSliderRow(
        title = "Bufor audio (Wi-Fi)",
        valueLabel = "${settings.carPlayAudioBufferWifiMs} ms",
        value = settings.carPlayAudioBufferWifiMs.toFloat(),
        valueRange = 60f..500f,
        onValueChange = { v -> onUpdateSettings { it.copy(carPlayAudioBufferWifiMs = v.roundToInt()) } }
    )
    CarOptionSelector(
        title = "Sieć bezprzewodowa",
        options = WirelessTransportMode.entries,
        selected = settings.carPlayWirelessMode,
        labelMapper = { it.labelPl },
        onSelect = { mode -> onUpdateSettings { it.copy(carPlayWirelessMode = mode) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
}

@Composable
private fun AndroidAutoSettingsTab(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onRunWirelessDiagnostics: () -> Unit
) {
    SectionHeader("Ustawienia Android Auto")
    CarOptionSelector(
        title = "Maksymalna rozdzielczość",
        options = VideoResolutionPreset.entries,
        selected = settings.aaResolution,
        labelMapper = { it.label },
        onSelect = { preset -> onUpdateSettings { it.copy(aaResolution = preset) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Automatyczne DPI",
        checked = settings.aaAutoDpi,
        onCheckedChange = { v -> onUpdateSettings { it.copy(aaAutoDpi = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    if (!settings.aaAutoDpi) {
        CarSliderRow(
            title = "Ręczne DPI interfejsu",
            valueLabel = "${settings.aaManualDpi} DPI",
            value = settings.aaManualDpi.toFloat(),
            valueRange = 140f..320f,
            onValueChange = { v -> onUpdateSettings { it.copy(aaManualDpi = v.roundToInt()) } }
        )
    }
    CarOptionSelector(
        title = "Liczba klatek (FPS)",
        options = listOf(30, 60),
        selected = settings.aaFps,
        labelMapper = { "$it FPS" },
        onSelect = { fps -> onUpdateSettings { it.copy(aaFps = fps) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Płynne wideo",
        checked = settings.aaSmoothVideo,
        onCheckedChange = { v -> onUpdateSettings { it.copy(aaSmoothVideo = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarOptionSelector(
        title = "Źródło mikrofonu",
        options = MicSource.entries,
        selected = settings.aaMicSource,
        labelMapper = { it.labelPl },
        onSelect = { src -> onUpdateSettings { it.copy(aaMicSource = src) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarOptionSelector(
        title = "Sieć bezprzewodowa",
        options = WirelessTransportMode.entries,
        selected = settings.aaWirelessMode,
        labelMapper = { it.labelPl },
        onSelect = { mode -> onUpdateSettings { it.copy(aaWirelessMode = mode) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSliderRow(
        title = "Bufor audio (kabel)",
        valueLabel = "${settings.aaAudioBufferCableMs} ms",
        value = settings.aaAudioBufferCableMs.toFloat(),
        valueRange = 20f..300f,
        onValueChange = { v -> onUpdateSettings { it.copy(aaAudioBufferCableMs = v.roundToInt()) } }
    )
    CarSliderRow(
        title = "Bufor audio (bezprzewodowo)",
        valueLabel = "${settings.aaAudioBufferWifiMs} ms",
        value = settings.aaAudioBufferWifiMs.toFloat(),
        valueRange = 60f..500f,
        onValueChange = { v -> onUpdateSettings { it.copy(aaAudioBufferWifiMs = v.roundToInt()) } }
    )
    Spacer(modifier = Modifier.height(4.dp))
    CarPrimaryButton(
        text = "Diagnostyka połączenia bezprzewodowego",
        icon = Icons.Rounded.Settings,
        onClick = onRunWirelessDiagnostics,
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
}

@Composable
private fun AutoKitSettingsTab(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    SectionHeader("Adaptery USB (Carlinkit / AutoKit)")
    Text(
        text = "Współpraca z zewnętrznymi przystawkami USB (wymagane m.in. dla sprzętowego uwierzytelniania Apple CarPlay). Pełny dekoder strumienia adaptera dodamy w Etapie 2.",
        style = MaterialTheme.typography.bodyLarge,
        color = CarPalette.TextSecondary
    )
    CarSwitchRow(
        title = "Automatyczne łączenie z adapterem USB",
        checked = settings.autoKitAutoConnect,
        onCheckedChange = { v -> onUpdateSettings { it.copy(autoKitAutoConnect = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Dźwięk przez Bluetooth samochodu",
        checked = settings.autoKitAudioViaCarBt,
        onCheckedChange = { v -> onUpdateSettings { it.copy(autoKitAudioViaCarBt = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Użyj mikrofonu telefonu",
        checked = settings.autoKitUsePhoneMic,
        onCheckedChange = { v -> onUpdateSettings { it.copy(autoKitUsePhoneMic = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarOptionSelector(
        title = "Docelowe FPS adaptera",
        options = listOf(30, 60),
        selected = settings.autoKitFps,
        labelMapper = { "$it FPS" },
        onSelect = { fps -> onUpdateSettings { it.copy(autoKitFps = fps) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Tryb Passthrough",
        subtitle = "Przekazuje surowe ramki USB wprost do silnika projekcji (dla wspieranych adapterów).",
        checked = settings.autoKitPassthroughMode,
        onCheckedChange = { v -> onUpdateSettings { it.copy(autoKitPassthroughMode = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
}

@Composable
private fun PanelSettingsTab(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    SectionHeader("Panel kierowcy i integracja z autem")
    Text(
        text = "Konfiguracja zachowania ekranu zegarów (klaster) i kamer bocznych. Pełna komunikacja z magistralą pojazdu wymaga testów na docelowym aucie.",
        style = MaterialTheme.typography.bodyLarge,
        color = CarPalette.TextSecondary
    )
    CarSwitchRow(
        title = "Używaj panelu kierowcy (obszar minimapy)",
        checked = settings.panelEnabled,
        onCheckedChange = { v -> onUpdateSettings { it.copy(panelEnabled = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Odbieraj wskazówki nawigacji i muzykę z telefonu",
        checked = settings.panelReceiveNavAndMedia,
        onCheckedChange = { v -> onUpdateSettings { it.copy(panelReceiveNavAndMedia = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Pokazuj minimapę na zegarach",
        checked = settings.panelShowMinimap,
        onCheckedChange = { v -> onUpdateSettings { it.copy(panelShowMinimap = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarOptionSelector(
        title = "Priorytet przy starcie",
        options = PanelPriority.entries,
        selected = settings.panelPriority,
        labelMapper = { it.labelPl },
        onSelect = { p -> onUpdateSettings { it.copy(panelPriority = p) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Ogłaszaj zmianę utworu przez 5 sekund",
        checked = settings.panelAnnounceTrackChange5s,
        onCheckedChange = { v -> onUpdateSettings { it.copy(panelAnnounceTrackChange5s = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Pokazuj kartę statystyk na panelu",
        checked = settings.panelShowStatsCard,
        onCheckedChange = { v -> onUpdateSettings { it.copy(panelShowStatsCard = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSliderRow(
        title = "Automatyczna zmiana ekranów panelu",
        valueLabel = if (settings.panelAutoCycleSeconds == 0) "Wyłączona" else "Co ${settings.panelAutoCycleSeconds} s",
        value = settings.panelAutoCycleSeconds.toFloat(),
        valueRange = 0f..60f,
        steps = 5,
        onValueChange = { v ->
            val snapped = ((v / 10f).roundToInt() * 10).coerceIn(0, 60)
            onUpdateSettings { it.copy(panelAutoCycleSeconds = snapped) }
        }
    )
    CarSwitchRow(
        title = "Podgląd kamer bocznych po włączeniu kierunkowskazu",
        checked = settings.sideCameraOnTurnSignal,
        onCheckedChange = { v -> onUpdateSettings { it.copy(sideCameraOnTurnSignal = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
    CarSwitchRow(
        title = "Wzorzec testowy (siatka kalibracyjna 20 dp)",
        checked = settings.panelCalibrationGridEnabled,
        onCheckedChange = { v -> onUpdateSettings { it.copy(panelCalibrationGridEnabled = v) } },
        uiSoundsEnabled = settings.uiSoundsEnabled
    )
}

@Composable
private fun AboutSettingsTab(
    settings: AppSettings,
    availableUpdate: VerifiedGitHubRelease?,
    onCheckUpdates: () -> Unit,
    onOpenLogs: () -> Unit
) {
    SectionHeader("O aplikacji MarolLink")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CarPalette.SurfaceElevated,
        border = BorderStroke(1.dp, CarPalette.Border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "MarolLink v${BuildConfig.VERSION_NAME} (kod ${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Niezależna, lokalna aplikacja samochodowa napisana od zera (clean-room). Działa bez żadnych zewnętrznych kont, kodów QR ani serwerów aktywacyjnych.",
                style = MaterialTheme.typography.bodyLarge,
                color = CarPalette.TextSecondary
            )
            Text(
                text = "Źródło aktualizacji (GitHub Releases): https://github.com/${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CarPrimaryButton(
            text = if (availableUpdate != null) {
                "Zainstaluj aktualizację ${availableUpdate.tagName}"
            } else {
                "Sprawdź aktualizacje"
            },
            icon = Icons.Rounded.SystemUpdate,
            onClick = onCheckUpdates,
            uiSoundsEnabled = settings.uiSoundsEnabled
        )
        CarSecondaryButton(
            text = "Zobacz lokalny dziennik zdarzeń",
            icon = Icons.Rounded.BugReport,
            onClick = onOpenLogs,
            uiSoundsEnabled = settings.uiSoundsEnabled
        )
    }
}

@Composable
private fun SteeringWheelMapperDialog(
    settings: AppSettings,
    onAssignKey: (SteeringAction, Int) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    var awaitingAction by remember { mutableStateOf<SteeringAction?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(awaitingAction) {
        if (awaitingAction != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 520.dp, max = 720.dp)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    val targetAction = awaitingAction
                    if (targetAction != null && event.type == KeyEventType.KeyDown) {
                        val nativeCode = event.nativeKeyEvent.keyCode
                        if (nativeCode != AndroidKeyEvent.KEYCODE_BACK) {
                            onAssignKey(targetAction, nativeCode)
                            awaitingAction = null
                            return@onPreviewKeyEvent true
                        }
                    }
                    false
                },
            shape = RoundedCornerShape(20.dp),
            color = CarPalette.Surface,
            border = BorderStroke(1.5.dp, CarPalette.Border)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Przyciski na kierownicy", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (awaitingAction != null) {
                        "Naciśnij teraz wybrany przycisk na kierownicy, aby przypisać: ${awaitingAction!!.labelPl}…"
                    } else {
                        "Wybierz akcję poniżej, a potem naciśnij fizyczny przycisk na kierownicy."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (awaitingAction != null) MaterialTheme.colorScheme.primary else CarPalette.TextSecondary
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SteeringAction.entries) { action ->
                        val assignedCode = settings.steeringMappings[action] ?: 0
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = CarPalette.SurfaceElevated,
                            border = BorderStroke(1.dp, CarPalette.Border)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(action.labelPl, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Klawisz: ${AndroidKeyEvent.keyCodeToString(assignedCode)} ($assignedCode)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CarPalette.TextSecondary
                                    )
                                }
                                CarSecondaryButton(
                                    text = if (awaitingAction == action) "Czekam…" else "Przypisz",
                                    onClick = { awaitingAction = action },
                                    uiSoundsEnabled = settings.uiSoundsEnabled
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CarSecondaryButton(
                        text = "Przywróć domyślne przyciski",
                        onClick = onResetDefaults,
                        uiSoundsEnabled = settings.uiSoundsEnabled
                    )
                    CarPrimaryButton(
                        text = "Gotowe",
                        onClick = onDismiss,
                        uiSoundsEnabled = settings.uiSoundsEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedDiagnosticsDialog(
    settings: AppSettings,
    entries: List<DiagnosticEntry>,
    onShareLogs: () -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeSection by rememberSaveable { mutableIntStateOf(0) }
    val sections = listOf("System", "Network", "Car", "Logs")

    Dialog(onDismissRequest = onDismiss) {
        CarCard(modifier = Modifier.widthIn(min = 560.dp, max = 780.dp)) {
            Text("Diagnostyka zaawansowana", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sections.forEachIndexed { index, title ->
                    val selected = activeSection == index
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { activeSection = index },
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else CarPalette.SurfaceElevated,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else CarPalette.Border)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarPalette.Background)
                    .padding(12.dp)
            ) {
                when (activeSection) {
                    0 -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Wersja: ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})")
                        Text("Model urządzenia: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                        Text("Android API: ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.VERSION.RELEASE})")
                        Text("Architektura ABI: ${android.os.Build.SUPPORTED_ABIS.joinToString()}")
                    }
                    1 -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Repozytorium OTA: https://github.com/${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO}")
                        Text("Tryb Wi-Fi CarPlay: ${settings.carPlayWirelessMode.labelPl}")
                        Text("Tryb Wi-Fi Android Auto: ${settings.aaWirelessMode.labelPl}")
                    }
                    2 -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Panel kierowcy: ${if (settings.panelEnabled) "Włączony" else "Wyłączony"}")
                        Text("Kamery boczne na kierunkowskaz: ${if (settings.sideCameraOnTurnSignal) "Włączone" else "Wyłączone"}")
                        Text("Marginesy (T/B/L/R): ${settings.safeAreaTopDp}/${settings.safeAreaBottomDp}/${settings.safeAreaLeftDp}/${settings.safeAreaRightDp} dp")
                    }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(entries.asReversed()) { entry ->
                            Text(
                                text = "${entry.timestamp} [${entry.level}/${entry.tag}] ${entry.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (entry.level == "ERROR") CarPalette.Danger else CarPalette.TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CarSecondaryButton(
                        text = "Udostępnij logi",
                        onClick = onShareLogs,
                        uiSoundsEnabled = settings.uiSoundsEnabled
                    )
                    CarSecondaryButton(
                        text = "Wyczyść",
                        onClick = onClearLogs,
                        uiSoundsEnabled = settings.uiSoundsEnabled
                    )
                }
                CarPrimaryButton(
                    text = "Zamknij",
                    onClick = onDismiss,
                    uiSoundsEnabled = settings.uiSoundsEnabled
                )
            }
        }
    }
}

@Composable
private fun WirelessDiagnosticsDialog(
    checks: List<WirelessDiagnosticCheck>,
    uiSoundsEnabled: Boolean,
    onRerun: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CarCard(modifier = Modifier.widthIn(min = 500.dp, max = 680.dp)) {
            Text("Diagnostyka połączenia bezprzewodowego", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(checks) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarPalette.SurfaceElevated)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.passed) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = if (item.passed) CarPalette.Success else CarPalette.Warning
                        )
                        Column {
                            Text(item.titlePl, style = MaterialTheme.typography.titleMedium)
                            Text(item.detailPl, style = MaterialTheme.typography.bodyMedium, color = CarPalette.TextSecondary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CarSecondaryButton(
                    text = "Sprawdź ponownie",
                    icon = Icons.Rounded.Refresh,
                    onClick = onRerun,
                    uiSoundsEnabled = uiSoundsEnabled
                )
                CarPrimaryButton(
                    text = "Zamknij",
                    onClick = onDismiss,
                    uiSoundsEnabled = uiSoundsEnabled
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary
    )
}
