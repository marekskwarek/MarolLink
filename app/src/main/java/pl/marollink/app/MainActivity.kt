package pl.marollink.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.marollink.app.connectivity.ProjectionSessionState
import pl.marollink.app.ui.components.OtaUpdateDialog
import pl.marollink.app.ui.screens.BluetoothPairingScreen
import pl.marollink.app.ui.screens.HomeScreen
import pl.marollink.app.ui.screens.OnboardingTipsScreen
import pl.marollink.app.ui.screens.ProjectionScreen
import pl.marollink.app.ui.screens.SafeAreaEditorScreen
import pl.marollink.app.ui.screens.SettingsScreen
import pl.marollink.app.ui.screens.TermsScreen
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.ui.theme.MarolLinkTheme

private enum class ScreenRoute {
    HOME,
    BLUETOOTH,
    PROJECTION,
    SETTINGS,
    SAFE_AREA
}

class MainActivity : ComponentActivity() {

    private val viewModel: MarolLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val settingsState by viewModel.settings.collectAsStateWithLifecycle()
            val knownDevices by viewModel.knownDevices.collectAsStateWithLifecycle()
            val usbDevices by viewModel.usbDevices.collectAsStateWithLifecycle()
            val pairedBt by viewModel.pairedBtDevices.collectAsStateWithLifecycle()
            val nearbyBt by viewModel.nearbyBtDevices.collectAsStateWithLifecycle()
            val isScanningBt by viewModel.isScanningBt.collectAsStateWithLifecycle()
            val btStatus by viewModel.btStatusMessage.collectAsStateWithLifecycle()
            val projectionState by viewModel.projectionState.collectAsStateWithLifecycle()
            val usbConflict by viewModel.usbConflictCandidate.collectAsStateWithLifecycle()
            val updateUiState by viewModel.updateUiState.collectAsStateWithLifecycle()
            val availableUpdate by viewModel.availableUpdate.collectAsStateWithLifecycle()
            val permissions by viewModel.permissions.collectAsStateWithLifecycle()
            val diagnosticEntries by viewModel.diagnosticEntries.collectAsStateWithLifecycle()
            val wirelessChecks by viewModel.wirelessChecks.collectAsStateWithLifecycle()

            val currentSettings = settingsState
            if (currentSettings == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CarPalette.Background)
                )
                return@setContent
            }

            var currentRoute by rememberSaveable { mutableStateOf(ScreenRoute.HOME) }
            var routeBeforeSettings by rememberSaveable { mutableStateOf(ScreenRoute.HOME) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) {
                viewModel.refreshPermissions()
            }

            val folderPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                if (uri != null) {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    runCatching {
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                    }
                    viewModel.setCallRecordingFolderUri(uri)
                }
            }

            LaunchedEffect(currentSettings.fullscreenMode) {
                applyFullscreenMode(currentSettings.fullscreenMode)
            }

            // Automatically open Projection screen when a session starts; return Home on disconnect
            LaunchedEffect(projectionState) {
                if (projectionState !is ProjectionSessionState.Disconnected &&
                    currentRoute != ScreenRoute.SETTINGS &&
                    currentRoute != ScreenRoute.SAFE_AREA
                ) {
                    currentRoute = ScreenRoute.PROJECTION
                } else if (projectionState is ProjectionSessionState.Disconnected &&
                    currentRoute == ScreenRoute.PROJECTION
                ) {
                    currentRoute = ScreenRoute.HOME
                }
            }

            MarolLinkTheme(accentHue = currentSettings.accentHue) {
                when {
                    !currentSettings.termsAccepted -> {
                        TermsScreen(
                            uiSoundsEnabled = currentSettings.uiSoundsEnabled,
                            onAccept = { viewModel.acceptTerms() }
                        )
                    }

                    !currentSettings.onboardingTipsCompleted -> {
                        OnboardingTipsScreen(
                            uiSoundsEnabled = currentSettings.uiSoundsEnabled,
                            onFinish = { viewModel.completeOnboarding() }
                        )
                    }

                    else -> {
                        when (currentRoute) {
                            ScreenRoute.HOME -> {
                                HomeScreen(
                                    settings = currentSettings,
                                    knownDevices = knownDevices,
                                    usbDevices = usbDevices,
                                    availableUpdate = availableUpdate,
                                    onConnectWireless = {
                                        viewModel.bluetoothHelper.refreshPairedDevices()
                                        currentRoute = ScreenRoute.BLUETOOTH
                                    },
                                    onConnectKnownDevice = { device ->
                                        viewModel.connectKnownDevice(device)
                                        currentRoute = ScreenRoute.PROJECTION
                                    },
                                    onForgetKnownDevice = { viewModel.forgetKnownDevice(it) },
                                    onConnectUsbDevice = { usb ->
                                        viewModel.connectUsbDevice(usb)
                                        if (usb.hasPermission) {
                                            currentRoute = ScreenRoute.PROJECTION
                                        }
                                    },
                                    onOpenSettings = {
                                        routeBeforeSettings = ScreenRoute.HOME
                                        viewModel.refreshPermissions()
                                        currentRoute = ScreenRoute.SETTINGS
                                    },
                                    onOpenUpdateOrCheck = {
                                        viewModel.otaUpdateManager.showAvailableDialogIfAny()
                                    }
                                )
                            }

                            ScreenRoute.BLUETOOTH -> {
                                BluetoothPairingScreen(
                                    pairedDevices = pairedBt,
                                    nearbyDevices = nearbyBt,
                                    isScanning = isScanningBt,
                                    statusMessage = btStatus,
                                    uiSoundsEnabled = currentSettings.uiSoundsEnabled,
                                    onSelectDevice = { bt ->
                                        viewModel.connectBluetoothDevice(bt)
                                        currentRoute = ScreenRoute.PROJECTION
                                    },
                                    onSearchAgain = {
                                        val missing = viewModel.missingPermissions()
                                        if (missing.isNotEmpty()) {
                                            permissionLauncher.launch(missing)
                                        }
                                        viewModel.bluetoothHelper.startDiscovery()
                                    },
                                    onOpenSystemBluetooth = {
                                        viewModel.openSystemBluetoothSettings()
                                    },
                                    onBack = {
                                        viewModel.bluetoothHelper.stopDiscovery()
                                        currentRoute = ScreenRoute.HOME
                                    }
                                )
                            }

                            ScreenRoute.PROJECTION -> {
                                ProjectionScreen(
                                    settings = currentSettings,
                                    sessionState = projectionState,
                                    knownDevices = knownDevices,
                                    conflictUsbCandidate = usbConflict,
                                    onCancelOrDisconnect = {
                                        viewModel.projectionBridge.disconnect()
                                        currentRoute = ScreenRoute.HOME
                                    },
                                    onReconnect = {
                                        viewModel.projectionBridge.reconnectActive()
                                    },
                                    onOpenSettings = {
                                        routeBeforeSettings = ScreenRoute.PROJECTION
                                        viewModel.refreshPermissions()
                                        currentRoute = ScreenRoute.SETTINGS
                                    },
                                    onSwitchToDevice = { dev ->
                                        viewModel.connectKnownDevice(dev)
                                    },
                                    onForgetDevice = { dev ->
                                        viewModel.forgetKnownDevice(dev)
                                    },
                                    onResolveConflictUseCable = {
                                        viewModel.projectionBridge.resolveConflictUseCable()
                                    },
                                    onResolveConflictKeepWireless = {
                                        viewModel.projectionBridge.resolveConflictKeepWireless()
                                    },
                                    onFourFingerTripleTap = {
                                        viewModel.logManager.addUserMarker()
                                    }
                                )
                            }

                            ScreenRoute.SETTINGS -> {
                                SettingsScreen(
                                    settings = currentSettings,
                                    permissions = permissions,
                                    diagnosticEntries = diagnosticEntries,
                                    wirelessChecks = wirelessChecks,
                                    availableUpdate = availableUpdate,
                                    onUpdateSettings = { viewModel.updateSettings(it) },
                                    onClearDevices = { viewModel.clearAllDevices() },
                                    onResetSettings = { viewModel.resetSettings() },
                                    onRequestMissingPermissions = {
                                        val missing = viewModel.missingPermissions()
                                        if (missing.isNotEmpty()) {
                                            permissionLauncher.launch(missing)
                                        } else {
                                            viewModel.openAppSystemSettings()
                                        }
                                    },
                                    onOpenAppSystemSettings = { viewModel.openAppSystemSettings() },
                                    onPickRecordingFolder = { folderPickerLauncher.launch(null) },
                                    onRunWirelessDiagnostics = { viewModel.runWirelessDiagnostics() },
                                    onShareLogs = {
                                        viewModel.createShareLogsIntent()?.let { shareIntent ->
                                            runCatching {
                                                startActivity(Intent.createChooser(shareIntent, "Udostępnij logi MarolLink"))
                                            }
                                        }
                                    },
                                    onClearLogs = { viewModel.logManager.clearLogs() },
                                    onOpenSafeAreaEditor = { currentRoute = ScreenRoute.SAFE_AREA },
                                    onCheckUpdates = { viewModel.otaUpdateManager.showAvailableDialogIfAny() },
                                    onBack = { currentRoute = routeBeforeSettings }
                                )
                            }

                            ScreenRoute.SAFE_AREA -> {
                                SafeAreaEditorScreen(
                                    settings = currentSettings,
                                    onUpdateMargins = { top, bottom, left, right ->
                                        viewModel.updateSettings {
                                            it.copy(
                                                safeAreaTopDp = top,
                                                safeAreaBottomDp = bottom,
                                                safeAreaLeftDp = left,
                                                safeAreaRightDp = right
                                            )
                                        }
                                    },
                                    onToggleGrid = { enabled ->
                                        viewModel.updateSettings {
                                            it.copy(panelCalibrationGridEnabled = enabled)
                                        }
                                    },
                                    onBack = { currentRoute = ScreenRoute.SETTINGS }
                                )
                            }
                        }
                    }
                }

                OtaUpdateDialog(
                    state = updateUiState,
                    uiSoundsEnabled = currentSettings.uiSoundsEnabled,
                    onStartUpdate = { release ->
                        viewModel.otaUpdateManager.startDownloadAndInstall(release)
                    },
                    onDismiss = { dismissedTag ->
                        viewModel.dismissUpdateDialog(dismissedTag)
                    },
                    onOpenInstallPermissions = {
                        viewModel.otaUpdateManager.openPermissionSettings()
                    },
                    onRelaunchInstaller = {
                        viewModel.otaUpdateManager.launchPendingInstall()
                    },
                    onRetry = { release ->
                        if (release != null) {
                            viewModel.otaUpdateManager.startDownloadAndInstall(release)
                        } else {
                            viewModel.otaUpdateManager.checkManually()
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeRefresh()
    }

    private fun applyFullscreenMode(fullscreen: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (fullscreen) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
