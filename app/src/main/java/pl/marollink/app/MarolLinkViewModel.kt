package pl.marollink.app

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.marollink.app.connectivity.AttachedUsbInfo
import pl.marollink.app.connectivity.BluetoothManagerHelper
import pl.marollink.app.connectivity.DiscoveredBluetoothDevice
import pl.marollink.app.connectivity.PermissionHelper
import pl.marollink.app.connectivity.PermissionStatusItem
import pl.marollink.app.connectivity.ProjectionBridge
import pl.marollink.app.connectivity.ProjectionSessionState
import pl.marollink.app.connectivity.UsbDeviceMonitor
import pl.marollink.app.connectivity.WirelessDiagnosticCheck
import pl.marollink.app.connectivity.WirelessDiagnosticsRunner
import pl.marollink.app.data.AppSettings
import pl.marollink.app.data.DeviceTransportType
import pl.marollink.app.data.DiagnosticEntry
import pl.marollink.app.data.DiagnosticLogManager
import pl.marollink.app.data.KnownDevice
import pl.marollink.app.data.ProjectionProtocol
import pl.marollink.app.data.SettingsRepository
import pl.marollink.app.updater.OtaUpdateManager
import pl.marollink.app.updater.UpdateUiState
import pl.marollink.app.updater.VerifiedGitHubRelease

class MarolLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    val logManager = DiagnosticLogManager(appContext)
    val otaUpdateManager = OtaUpdateManager(appContext, logManager, viewModelScope)
    val usbMonitor = UsbDeviceMonitor(appContext, logManager)
    val bluetoothHelper = BluetoothManagerHelper(appContext, logManager)
    val projectionBridge = ProjectionBridge(settingsRepository, logManager, viewModelScope)

    val settings: StateFlow<AppSettings?> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val knownDevices: StateFlow<List<KnownDevice>> = settingsRepository.knownDevicesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val usbDevices: StateFlow<List<AttachedUsbInfo>> = usbMonitor.attachedDevices
    val pairedBtDevices: StateFlow<List<DiscoveredBluetoothDevice>> = bluetoothHelper.pairedDevices
    val nearbyBtDevices: StateFlow<List<DiscoveredBluetoothDevice>> = bluetoothHelper.nearbyDevices
    val isScanningBt: StateFlow<Boolean> = bluetoothHelper.isScanning
    val btStatusMessage: StateFlow<String?> = bluetoothHelper.statusMessage

    val projectionState: StateFlow<ProjectionSessionState> = projectionBridge.sessionState
    val usbConflictCandidate: StateFlow<AttachedUsbInfo?> = projectionBridge.conflictUsbCandidate
    val updateUiState: StateFlow<UpdateUiState> = otaUpdateManager.state
    val availableUpdate: StateFlow<VerifiedGitHubRelease?> = otaUpdateManager.latestAvailableRelease
    val diagnosticEntries: StateFlow<List<DiagnosticEntry>> = logManager.entries

    private val _permissions = MutableStateFlow(PermissionHelper.inspectAll(appContext))
    val permissions: StateFlow<List<PermissionStatusItem>> = _permissions.asStateFlow()

    private val _wirelessChecks = MutableStateFlow<List<WirelessDiagnosticCheck>>(emptyList())
    val wirelessChecks: StateFlow<List<WirelessDiagnosticCheck>> = _wirelessChecks.asStateFlow()

    init {
        usbMonitor.onUsbAttachedEvent = { usbInfo ->
            val autoConnect = settings.value?.autoConnectUsb ?: true
            projectionBridge.onUsbAttachedDuringRuntime(usbInfo, autoConnect)
        }
        usbMonitor.start()
        refreshPermissions()

        viewModelScope.launch {
            val initialSettings = settingsRepository.settingsFlow.first()
            otaUpdateManager.checkOnStartupIfOnline(initialSettings.dismissedUpdateTag)
        }
    }

    fun onResumeRefresh() {
        usbMonitor.refreshDevices()
        refreshPermissions()
        otaUpdateManager.resumePendingInstallIfPermitted()
    }

    fun refreshPermissions() {
        _permissions.value = PermissionHelper.inspectAll(appContext)
    }

    fun missingPermissions(): Array<String> = PermissionHelper.missingRuntimePermissions(appContext)

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(transform)
        }
    }

    fun acceptTerms() = updateSettings { it.copy(termsAccepted = true) }

    fun completeOnboarding() = updateSettings { it.copy(onboardingTipsCompleted = true) }

    fun dismissUpdateDialog(dismissedTag: String?) {
        if (!dismissedTag.isNullOrBlank()) {
            updateSettings { it.copy(dismissedUpdateTag = dismissedTag) }
        }
        otaUpdateManager.dismissDialog()
    }

    fun connectUsbDevice(usbInfo: AttachedUsbInfo) {
        if (!usbInfo.hasPermission) {
            usbMonitor.requestPermission(usbInfo.deviceName)
            return
        }
        projectionBridge.connectDevice(projectionBridge.toKnownDevice(usbInfo))
    }

    fun connectBluetoothDevice(btDevice: DiscoveredBluetoothDevice) {
        bluetoothHelper.stopDiscovery()
        val device = KnownDevice(
            id = "bt:${btDevice.address}",
            name = btDevice.name,
            addressOrSerial = btDevice.address,
            transportType = DeviceTransportType.BLUETOOTH,
            protocol = ProjectionProtocol.UNKNOWN,
            lastConnectedEpochMs = System.currentTimeMillis()
        )
        projectionBridge.connectDevice(device)
    }

    fun connectKnownDevice(device: KnownDevice) {
        projectionBridge.connectDevice(device)
    }

    fun forgetKnownDevice(device: KnownDevice) {
        viewModelScope.launch {
            settingsRepository.removeDevice(device.id)
            logManager.log("INFO", "Devices", "Zapomniano urządzenie: ${device.name}")
        }
    }

    fun clearAllDevices() {
        viewModelScope.launch {
            settingsRepository.clearDevices()
            logManager.log("INFO", "Devices", "Wyczyszczono listę zapisanych urządzeń")
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetSettingsKeepingDevices()
            logManager.log("INFO", "Settings", "Przywrócono domyślne ustawienia (zachowano sparowane urządzenia)")
        }
    }

    fun setCallRecordingFolderUri(uri: Uri?) {
        updateSettings { it.copy(callRecordingFolderUri = uri?.toString()) }
        if (uri != null) {
            logManager.log("INFO", "Audio", "Ustawiono folder nagrań WAV: $uri")
        }
    }

    fun runWirelessDiagnostics() {
        _wirelessChecks.value = WirelessDiagnosticsRunner.runChecks(appContext)
        logManager.log("INFO", "Diagnostics", "Wykonano diagnostykę połączenia bezprzewodowego")
    }

    fun openSystemBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { appContext.startActivity(intent) }
    }

    fun openAppSystemSettings() {
        PermissionHelper.openAppSettings(appContext)
    }

    fun createShareLogsIntent(): Intent? {
        val logFile = logManager.getLogFile()
        if (!logFile.exists()) return null
        val uri = runCatching {
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                logFile
            )
        }.getOrNull() ?: return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MarolLink - logi diagnostyczne")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun onCleared() {
        usbMonitor.stop()
        bluetoothHelper.stopDiscovery()
        super.onCleared()
    }
}
