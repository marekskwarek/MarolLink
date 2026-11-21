package pl.marollink.app.connectivity

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager

data class WirelessDiagnosticCheck(
    val titlePl: String,
    val passed: Boolean,
    val detailPl: String
)

object WirelessDiagnosticsRunner {

    fun runChecks(context: Context): List<WirelessDiagnosticCheck> {
        val pm = context.packageManager
        val btAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        val hasBtHardware = btAdapter != null
        val btEnabled = btAdapter?.isEnabled == true
        val btPerm = PermissionHelper.hasBluetoothConnect(context) && PermissionHelper.hasBluetoothScan(context)

        val wifiEnabled = wifiManager?.isWifiEnabled == true
        val supports5Ghz = wifiManager?.is5GHzBandSupported == true
        val supportsWifiDirect = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)
        val supportsUsbHost = pm.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

        return listOf(
            WirelessDiagnosticCheck(
                titlePl = "Moduł Bluetooth centrali",
                passed = hasBtHardware && btEnabled,
                detailPl = when {
                    !hasBtHardware -> "Brak modułu Bluetooth w systemie"
                    btEnabled -> "Bluetooth włączony i gotowy"
                    else -> "Bluetooth jest wyłączony w ustawieniach Androida"
                }
            ),
            WirelessDiagnosticCheck(
                titlePl = "Uprawnienia Bluetooth i skanowania",
                passed = btPerm,
                detailPl = if (btPerm) "Przyznane" else "Wymagana zgoda użytkownika w Ustawieniach"
            ),
            WirelessDiagnosticCheck(
                titlePl = "Wi-Fi włączone (do Wi-Fi Direct 5 GHz)",
                passed = wifiEnabled,
                detailPl = if (wifiEnabled) {
                    "Wi-Fi włączone — tryb Wi-Fi Direct 5 GHz może działać"
                } else {
                    "Wi-Fi wyłączone — dobre dla własnego hotspotu 2.4 GHz, wymagane do Wi-Fi Direct"
                }
            ),
            WirelessDiagnosticCheck(
                titlePl = "Obsługa pasma 5 GHz Wi-Fi",
                passed = supports5Ghz,
                detailPl = if (supports5Ghz) "Centrala obsługuje 5 GHz" else "Tylko 2.4 GHz (lub brak danych)"
            ),
            WirelessDiagnosticCheck(
                titlePl = "Funkcja Wi-Fi Direct (P2P)",
                passed = supportsWifiDirect,
                detailPl = if (supportsWifiDirect) "Obsługiwana przez system" else "Niewykryta przez system"
            ),
            WirelessDiagnosticCheck(
                titlePl = "Host USB (kabel / adapter AutoKit)",
                passed = supportsUsbHost,
                detailPl = if (supportsUsbHost) "Port USB obsługuje urządzenia klienckie" else "Brak trybu USB Host"
            )
        )
    }
}
