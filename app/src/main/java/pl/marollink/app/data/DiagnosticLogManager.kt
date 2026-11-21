package pl.marollink.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticEntry(
    val timestamp: String,
    val level: String,
    val tag: String,
    val message: String
)

class DiagnosticLogManager(context: Context) {

    private val logDir: File = File(
        context.getExternalFilesDir(null) ?: context.filesDir,
        "logs"
    ).apply { mkdirs() }

    private val activeLogFile: File = File(logDir, "marollink-session.log")
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val _entries = MutableStateFlow<List<DiagnosticEntry>>(emptyList())
    val entries: StateFlow<List<DiagnosticEntry>> = _entries.asStateFlow()

    init {
        trimOldLogsIfNeeded()
        log("INFO", "App", "Uruchomiono lokalny dziennik diagnostyczny MarolLink")
    }

    @Synchronized
    fun log(level: String, tag: String, message: String) {
        val ts = timeFormat.format(Date())
        val entry = DiagnosticEntry(ts, level, tag, message)
        val updated = (_entries.value + entry).takeLast(250)
        _entries.value = updated
        runCatching {
            activeLogFile.appendText("$ts [$level/$tag] $message\n")
        }
    }

    fun addUserMarker() {
        log("MARKER", "Gesture", "Dodano ręczny znacznik diagnostyczny (4 palce x3)")
    }

    fun getLogFile(): File = activeLogFile

    fun clearLogs() {
        _entries.value = emptyList()
        runCatching {
            activeLogFile.writeText("")
        }
        log("INFO", "App", "Wyczyszczono lokalne logi diagnostyczne")
    }

    private fun trimOldLogsIfNeeded() {
        runCatching {
            if (activeLogFile.exists() && activeLogFile.length() > MAX_LOG_BYTES) {
                val backup = File(logDir, "marollink-session.prev.log")
                if (backup.exists()) backup.delete()
                activeLogFile.renameTo(backup)
            }
        }
    }

    companion object {
        private const val MAX_LOG_BYTES = 1024L * 1024L
    }
}
