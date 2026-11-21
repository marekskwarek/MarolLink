package pl.marollink.app.connectivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

data class PermissionStatusItem(
    val id: String,
    val titlePl: String,
    val descriptionPl: String,
    val manifestPermissions: List<String>,
    val granted: Boolean
)

object PermissionHelper {

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBluetoothConnect(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(context, Manifest.permission.BLUETOOTH)
        }
    }

    fun hasBluetoothScan(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun hasInstallPackagesPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun inspectAll(context: Context): List<PermissionStatusItem> {
        val btPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }

        val notifPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }

        return listOf(
            PermissionStatusItem(
                id = "bluetooth",
                titlePl = "Urządzenia w pobliżu (Bluetooth)",
                descriptionPl = "Wymagane do parowania i łączenia telefonu bezprzewodowo.",
                manifestPermissions = btPerms,
                granted = hasBluetoothConnect(context) && hasBluetoothScan(context)
            ),
            PermissionStatusItem(
                id = "location",
                titlePl = "Lokalizacja (Wi-Fi Direct / hotspot)",
                descriptionPl = "System Android wymaga jej do wyszukiwania urządzeń i sieci 5 GHz.",
                manifestPermissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
                granted = hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            ),
            PermissionStatusItem(
                id = "microphone",
                titlePl = "Mikrofon",
                descriptionPl = "Obsługa poleceń głosowych i rozmów przez centralę auta.",
                manifestPermissions = listOf(Manifest.permission.RECORD_AUDIO),
                granted = hasPermission(context, Manifest.permission.RECORD_AUDIO)
            ),
            PermissionStatusItem(
                id = "notifications",
                titlePl = "Powiadomienia",
                descriptionPl = "Status aktywnego połączenia w tle i powiadomienia o aktualizacjach.",
                manifestPermissions = notifPerms,
                granted = notifPerms.isEmpty() || notifPerms.all { hasPermission(context, it) }
            )
        )
    }

    fun missingRuntimePermissions(context: Context): Array<String> {
        return inspectAll(context)
            .filter { !it.granted }
            .flatMap { it.manifestPermissions }
            .distinct()
            .toTypedArray()
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
