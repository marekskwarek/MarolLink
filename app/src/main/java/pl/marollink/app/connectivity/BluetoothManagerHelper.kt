package pl.marollink.app.connectivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.marollink.app.data.DiagnosticLogManager

data class DiscoveredBluetoothDevice(
    val address: String,
    val name: String,
    val isBonded: Boolean
)

class BluetoothManagerHelper(
    private val context: Context,
    private val logManager: DiagnosticLogManager
) {
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val _pairedDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _nearbyDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _nearbyDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var receiverRegistered = false

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = extractBluetoothDevice(intent) ?: return
                    addDiscoveredDevice(device)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                    logManager.log("INFO", "Bluetooth", "Zakończono skanowanie Bluetooth")
                }
            }
        }
    }

    fun isAdapterAvailable(): Boolean = adapter != null

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    fun refreshPairedDevices() {
        val btAdapter = adapter
        if (btAdapter == null) {
            _statusMessage.value = "To urządzenie nie ma aktywnego modułu Bluetooth."
            _pairedDevices.value = emptyList()
            return
        }
        if (!btAdapter.isEnabled) {
            _statusMessage.value = "Bluetooth w centrali jest wyłączony."
            _pairedDevices.value = emptyList()
            return
        }
        if (!PermissionHelper.hasBluetoothConnect(context)) {
            _statusMessage.value = "Brak uprawnienia Bluetooth — przyznaj zgodę w Ustawieniach."
            _pairedDevices.value = emptyList()
            return
        }
        _pairedDevices.value = readBondedDevicesSafely(btAdapter)
        _statusMessage.value = null
    }

    @SuppressLint("MissingPermission")
    private fun readBondedDevicesSafely(btAdapter: BluetoothAdapter): List<DiscoveredBluetoothDevice> {
        return runCatching {
            btAdapter.bondedDevices.orEmpty().map { dev ->
                DiscoveredBluetoothDevice(
                    address = dev.address,
                    name = safeDeviceName(dev),
                    isBonded = true
                )
            }.sortedBy { it.name.lowercase() }
        }.getOrDefault(emptyList())
    }

    fun startDiscovery() {
        refreshPairedDevices()
        val btAdapter = adapter ?: return
        if (!btAdapter.isEnabled) {
            _statusMessage.value = "Włącz Bluetooth w systemie samochodu, aby wyszukać telefon."
            return
        }
        if (!PermissionHelper.hasBluetoothScan(context) || !PermissionHelper.hasBluetoothConnect(context)) {
            _statusMessage.value = "Wymagane są uprawnienia Bluetooth i Lokalizacji do skanowania urządzeń."
            return
        }
        registerDiscoveryReceiverIfNeeded()
        _nearbyDevices.value = emptyList()
        startDiscoverySafely(btAdapter)
    }

    @SuppressLint("MissingPermission")
    private fun startDiscoverySafely(btAdapter: BluetoothAdapter) {
        runCatching {
            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            }
            val started = btAdapter.startDiscovery()
            _isScanning.value = started
            if (!started) {
                _statusMessage.value = "System nie pozwolił uruchomić wyszukiwania Bluetooth."
            } else {
                _statusMessage.value = null
                logManager.log("INFO", "Bluetooth", "Rozpoczęto wyszukiwanie telefonów Bluetooth")
            }
        }.onFailure { err ->
            _isScanning.value = false
            _statusMessage.value = "Błąd uruchamiania skanowania Bluetooth: ${err.message}"
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (PermissionHelper.hasBluetoothScan(context)) {
            runCatching {
                if (adapter?.isDiscovering == true) {
                    adapter.cancelDiscovery()
                }
            }
        }
        _isScanning.value = false
        if (receiverRegistered) {
            runCatching { context.unregisterReceiver(discoveryReceiver) }
            receiverRegistered = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun addDiscoveredDevice(device: BluetoothDevice) {
        if (!PermissionHelper.hasBluetoothConnect(context)) return
        val item = DiscoveredBluetoothDevice(
            address = device.address,
            name = safeDeviceName(device),
            isBonded = device.bondState == BluetoothDevice.BOND_BONDED
        )
        val current = _nearbyDevices.value
        if (current.none { it.address == item.address }) {
            _nearbyDevices.value = current + item
        }
    }

    @SuppressLint("MissingPermission")
    private fun safeDeviceName(device: BluetoothDevice): String {
        val raw = runCatching { device.name }.getOrNull()
        return if (raw.isNullOrBlank()) "Urządzenie (${device.address})" else raw
    }

    private fun registerDiscoveryReceiverIfNeeded() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        ContextCompat.registerReceiver(context, discoveryReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
        receiverRegistered = true
    }

    @Suppress("DEPRECATION")
    private fun extractBluetoothDevice(intent: Intent): BluetoothDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
    }
}
