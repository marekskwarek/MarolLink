package pl.marollink.app.updater

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed interface DownloadResult {
    data class Success(val apkFile: File) : DownloadResult
    data class Failure(val reasonPl: String) : DownloadResult
}

class ApkDownloader(context: Context) {

    private val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }

    suspend fun downloadAndVerify(
        release: VerifiedGitHubRelease,
        onProgress: (percent: Int, downloadedBytes: Long, totalBytes: Long) -> Unit,
        onVerifying: () -> Unit
    ): DownloadResult = withContext(Dispatchers.IO) {
        cleanOldFiles()
        val safeFileName = release.apkFileName.replace(Regex("""[^A-Za-z0-9._-]"""), "_")
        val tempFile = File(updatesDir, "$safeFileName.part")
        val targetFile = File(updatesDir, safeFileName)

        var conn: HttpURLConnection? = null
        try {
            conn = (URL(release.apkUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 20_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "MarolLink-Android")
            }

            if (conn.responseCode !in 200..299) {
                return@withContext DownloadResult.Failure(
                    "Błąd pobierania pliku APK (HTTP ${conn.responseCode})."
                )
            }

            val totalBytes = conn.contentLengthLong.takeIf { it > 0 } ?: release.apkSizeBytes
            var downloadedBytes = 0L
            var lastReportedPercent = -1

            conn.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        val percent = if (totalBytes > 0) {
                            ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }
                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            onProgress(percent, downloadedBytes, totalBytes)
                        }
                    }
                    output.flush()
                }
            }

            onVerifying()
            if (!Sha256Verifier.verifyFile(tempFile, release.expectedSha256)) {
                tempFile.delete()
                return@withContext DownloadResult.Failure(
                    "Weryfikacja sumy kontrolnej SHA-256 nie powiodła się. Pobrany plik został odrzucony ze względów bezpieczeństwa."
                )
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            DownloadResult.Success(targetFile)
        } catch (e: IOException) {
            tempFile.delete()
            DownloadResult.Failure("Przerwano pobieranie aktualizacji (${e.message ?: "błąd sieci"}). ")
        } catch (e: Exception) {
            tempFile.delete()
            DownloadResult.Failure("Nieoczekiwany błąd podczas pobierania (${e.message ?: "błąd"}). ")
        } finally {
            conn?.disconnect()
        }
    }

    private fun cleanOldFiles() {
        updatesDir.listFiles()?.forEach { file ->
            file.delete()
        }
    }
}
