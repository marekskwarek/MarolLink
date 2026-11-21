package pl.marollink.app.connectivity

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.marollink.app.data.DiagnosticLogManager

data class AttachedUsbInfo(
    val deviceName: String,
    val displayTitle: String,
    val vendorId: Int,
    val productId: Int,
    val isAutoKitAdapter: Boolean,
    val hasPermission: Boolean
)

class UsbDeviceMonitor(
    private val context: Context,
    private val logManager: DiagnosticLogManager
) {
    private val usbManager: UsbManager? = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    private val _attachedDevices = MutableStateFlow<List<AttachedUsbInfo>>(emptyList())
    val attachedDevices: StateFlow<List<AttachedUsbInfo>> = _attachedDevices.asStateFlow()

    var onUsbAttachedEvent: ((AttachedUsbInfo) -> Unit)? = null
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = extractUsbDevice(intent)
                    refreshDevices()
                    device?.let { dev ->
                        val info = toInfo(dev)
                        logManager.log("INFO", "USB", "Podłączono USB: ${info.displayTitle} (VID=${hex(info.vendorId)}, PID=${hex(info.productId)})")
                        onUsbAttachedEvent?.invoke(info)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = extractUsbDevice(intent)
                    refreshDevices()
                    device?.let {
                        logManager.log("INFO", "USB", "Odłączono urządzenie USB: ${it.deviceName}")
                    }
                }
                ACTION_USB_PERMISSION -> {
                    refreshDevices()
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    logManager.log("INFO", "USB", "Uprawnienie USB: ${if (granted) "przyznane" else "odrzucone"}")
                }
            }
        }
    }

    fun start() {
        if (registered) return
        val systemFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(context, receiver, systemFilter, ContextCompat.RECEIVER_EXPORTED)

        val permFilter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(context, receiver, permFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        registered = true
        refreshDevices()
    }

    fun stop() {
        if (!registered) return
        runCatching { context.unregisterReceiver(receiver) }
        registered = false
    }

    fun refreshDevices() {
        val manager = usbManager ?: run {
            _attachedDevices.value = emptyList()
            return
        }
        val list = manager.deviceList.values.map { toInfo(it) }
        _attachedDevices.value = list
    }

    fun requestPermission(deviceName: String) {
        val manager = usbManager ?: return
        val device = manager.deviceList[deviceName] ?: return
        if (manager.hasPermission(device)) {
            refreshDevices()
            return
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val permIntent = Intent(ACTION_USB_PERMISSION).setPackage(context.packageName)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, permIntent, flags)
        manager.requestPermission(device, pendingIntent)
    }

    private fun toInfo(device: UsbDevice): AttachedUsbInfo {
        val isAutoKit = device.vendorId == AUTOKIT_VENDOR_ID &&
            device.productId in AUTOKIT_PRODUCT_IDS
        val productLabel = runCatching { device.productName }.getOrNull().orEmpty()
        val manufacturerLabel = runCatching { device.manufacturerName }.getOrNull().orEmpty()
        val title = when {
            isAutoKit -> "Adapter USB Carlinkit / AutoKit"
            productLabel.isNotBlank() -> listOf(manufacturerLabel, productLabel)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            else -> "Urządzenie USB (${hex(device.vendorId)}:${hex(device.productId)})"
        }
        return AttachedUsbInfo(
            deviceName = device.deviceName,
            displayTitle = title,
            vendorId = device.vendorId,
            productId = device.productId,
            isAutoKitAdapter = isAutoKit,
            hasPermission = usbManager?.hasPermission(device) == true
        )
    }

    @Suppress("DEPRECATION")
    private fun extractUsbDevice(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    private fun hex(value: Int): String = "0x%04X".format(value)

    companion object {
        private const val ACTION_USB_PERMISSION = "pl.marollink.app.USB_PERMISSION"
        private const val AUTOKIT_VENDOR_ID = 0x1314
        private val AUTOKIT_PRODUCT_IDS = setOf(0x1520, 0x1521, 0x1523)
    }
}
