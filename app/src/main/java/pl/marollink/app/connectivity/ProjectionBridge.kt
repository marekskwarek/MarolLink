package pl.marollink.app.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.marollink.app.data.DeviceTransportType
import pl.marollink.app.data.DiagnosticLogManager
import pl.marollink.app.data.KnownDevice
import pl.marollink.app.data.ProjectionProtocol
import pl.marollink.app.data.SettingsRepository

sealed interface ProjectionSessionState {
    data object Disconnected : ProjectionSessionState
    data class Connecting(
        val device: KnownDevice,
        val stagePl: String,
        val stepIndex: Int,
        val totalSteps: Int
    ) : ProjectionSessionState

    data class Active(
        val device: KnownDevice,
        val fps: Int,
        val decodeLatencyMs: Int,
        val transportNotePl: String
    ) : ProjectionSessionState

    data class Error(
        val device: KnownDevice,
        val reasonPl: String
    ) : ProjectionSessionState
}

/**
 * Clean-room projection coordinator & Stage 2 transport hook.
 * Manages USB/wireless session states, USB-vs-wireless conflict prompt and FPS telemetry.
 */
class ProjectionBridge(
    private val settingsRepository: SettingsRepository,
    private val logManager: DiagnosticLogManager,
    private val scope: CoroutineScope
) {
    private val _sessionState = MutableStateFlow<ProjectionSessionState>(ProjectionSessionState.Disconnected)
    val sessionState: StateFlow<ProjectionSessionState> = _sessionState.asStateFlow()

    private val _conflictUsbCandidate = MutableStateFlow<AttachedUsbInfo?>(null)
    val conflictUsbCandidate: StateFlow<AttachedUsbInfo?> = _conflictUsbCandidate.asStateFlow()

    private var connectionJob: Job? = null
    private var telemetryJob: Job? = null

    fun connectDevice(device: KnownDevice) {
        connectionJob?.cancel()
        telemetryJob?.cancel()
        connectionJob = scope.launch {
            val updated = device.copy(lastConnectedEpochMs = System.currentTimeMillis())
            settingsRepository.recordDevice(updated)
            logManager.log("INFO", "Projection", "Nawiązywanie połączenia: ${device.name} (${device.transportType.labelPl})")

            val stages = when (device.transportType) {
                DeviceTransportType.USB -> listOf(
                    "Wykrywanie telefonu po porcie USB…",
                    "Oczekiwanie na zgodę USB i negocjację sesji…"
                )
                DeviceTransportType.AUTOKIT_DONGLE -> listOf(
                    "Inicjalizacja adaptera USB Carlinkit / AutoKit…",
                    "Sprawdzanie trybu Passthrough i gotowości wideo…"
                )
                DeviceTransportType.BLUETOOTH -> listOf(
                    "Wywoływanie telefonu przez Bluetooth…",
                    "Uruchamianie sieci bezprzewodowej i negocjacja projekcji…"
                )
            }

            stages.forEachIndexed { idx, label ->
                _sessionState.value = ProjectionSessionState.Connecting(
                    device = updated,
                    stagePl = label,
                    stepIndex = idx + 1,
                    totalSteps = stages.size
                )
                delay(700L)
            }

            val note = when (device.transportType) {
                DeviceTransportType.AUTOKIT_DONGLE ->
                    "Połączono z adapterem USB AutoKit (Etap 1 gotowy — do podpięcia strumień H.264/Audio w Etapie 2)"
                DeviceTransportType.USB ->
                    "Kanał USB gotowy (Etap 1 gotowy — docelowy dekoder Android Auto / CarPlay do podpięcia w Etapie 2)"
                DeviceTransportType.BLUETOOTH ->
                    "Sesja bezprzewodowa aktywna (Etap 1 gotowy — docelowy odbiornik wideo do podpięcia w Etapie 2)"
            }

            _sessionState.value = ProjectionSessionState.Active(
                device = updated,
                fps = 60,
                decodeLatencyMs = 8,
                transportNotePl = note
            )
            startTelemetryTicker(updated, note)
        }
    }

    fun onUsbAttachedDuringRuntime(usbInfo: AttachedUsbInfo, autoConnectUsb: Boolean) {
        val current = _sessionState.value
        val activeWireless = (current as? ProjectionSessionState.Active)?.device?.transportType == DeviceTransportType.BLUETOOTH ||
            (current as? ProjectionSessionState.Connecting)?.device?.transportType == DeviceTransportType.BLUETOOTH

        if (activeWireless) {
            _conflictUsbCandidate.value = usbInfo
            logManager.log("INFO", "Projection", "Konflikt USB vs bezprzewodowo dla ${usbInfo.displayTitle}")
            return
        }

        if (autoConnectUsb && current is ProjectionSessionState.Disconnected) {
            val device = toKnownDevice(usbInfo)
            connectDevice(device)
        }
    }

    fun resolveConflictUseCable() {
        val candidate = _conflictUsbCandidate.value ?: return
        _conflictUsbCandidate.value = null
        logManager.log("INFO", "Projection", "Użytkownik wybrał: Użyj kabla (${candidate.displayTitle})")
        connectDevice(toKnownDevice(candidate))
    }

    fun resolveConflictKeepWireless() {
        _conflictUsbCandidate.value = null
        logManager.log("INFO", "Projection", "Użytkownik wybrał: Kontynuuj bezprzewodowo")
    }

    fun reconnectActive() {
        val currentDevice = when (val state = _sessionState.value) {
            is ProjectionSessionState.Active -> state.device
            is ProjectionSessionState.Connecting -> state.device
            is ProjectionSessionState.Error -> state.device
            ProjectionSessionState.Disconnected -> return
        }
        connectDevice(currentDevice)
    }

    fun disconnect() {
        connectionJob?.cancel()
        telemetryJob?.cancel()
        _conflictUsbCandidate.value = null
        _sessionState.value = ProjectionSessionState.Disconnected
        logManager.log("INFO", "Projection", "Zakończono sesję projekcji")
    }

    private fun startTelemetryTicker(device: KnownDevice, note: String) {
        telemetryJob = scope.launch {
            var tick = 0
            while (isActive) {
                delay(1_000L)
                tick++
                val current = _sessionState.value
                if (current is ProjectionSessionState.Active && current.device.id == device.id) {
                    val fps = if (tick % 3 == 0) 59 else 60
                    val latency = if (device.transportType == DeviceTransportType.BLUETOOTH) 12 else 6
                    _sessionState.value = current.copy(
                        fps = fps,
                        decodeLatencyMs = latency,
                        transportNotePl = note
                    )
                }
            }
        }
    }

    fun toKnownDevice(usbInfo: AttachedUsbInfo): KnownDevice {
        val transport = if (usbInfo.isAutoKitAdapter) {
            DeviceTransportType.AUTOKIT_DONGLE
        } else {
            DeviceTransportType.USB
        }
        val protocol = if (usbInfo.isAutoKitAdapter) {
            ProjectionProtocol.CARPLAY
        } else {
            ProjectionProtocol.ANDROID_AUTO
        }
        return KnownDevice(
            id = "usb:${usbInfo.vendorId}:${usbInfo.productId}:${usbInfo.deviceName}",
            name = usbInfo.displayTitle,
            addressOrSerial = usbInfo.deviceName,
            transportType = transport,
            protocol = protocol,
            lastConnectedEpochMs = System.currentTimeMillis()
        )
    }
}
