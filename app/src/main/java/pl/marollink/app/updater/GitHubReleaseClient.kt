package pl.marollink.app.updater

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class VerifiedGitHubRelease(
    val tagName: String,
    val version: SemanticVersion,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val expectedSha256: String
)

sealed interface ReleaseFetchResult {
    data class Success(val release: VerifiedGitHubRelease) : ReleaseFetchResult
    data object NoReleasesPublished : ReleaseFetchResult
    data class Failure(val reasonPl: String, val cause: Throwable? = null) : ReleaseFetchResult
}

class GitHubReleaseClient(
    private val owner: String,
    private val repo: String,
    private val userAgent: String
) {
    suspend fun fetchLatestRelease(): ReleaseFetchResult = withContext(Dispatchers.IO) {
        val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
        try {
            val jsonText = downloadText(
                url = apiUrl,
                acceptHeader = "application/vnd.github+json"
            ) ?: return@withContext ReleaseFetchResult.NoReleasesPublished

            val parsed = GitHubReleaseParser.parseReleaseJson(jsonText)
                ?: return@withContext ReleaseFetchResult.Failure(
                    "Wydanie na GitHubie nie zawiera poprawnego pliku APK."
                )

            val shaAsset = parsed.sha256Asset
                ?: return@withContext ReleaseFetchResult.Failure(
                    "Wydanie ${parsed.tagName} nie zawiera wymaganego pliku sumy kontrolnej SHA-256."
                )

            val shaText = downloadText(
                url = shaAsset.downloadUrl,
                acceptHeader = "text/plain"
            ) ?: return@withContext ReleaseFetchResult.Failure(
                "Nie udało się pobrać pliku sumy kontrolnej SHA-256 z GitHuba."
            )

            val expectedHash = GitHubReleaseParser.parseSha256Content(shaText, parsed.apkAsset.name)
                ?: return@withContext ReleaseFetchResult.Failure(
                    "Plik sumy kontrolnej SHA-256 ma nieprawidłowy format."
                )

            ReleaseFetchResult.Success(
                VerifiedGitHubRelease(
                    tagName = parsed.tagName,
                    version = parsed.version,
                    releaseTitle = parsed.releaseTitle,
                    releaseNotes = parsed.releaseNotes.ifBlank {
                        "Poprawki stabilności i nowe funkcje aplikacji MarolLink."
                    },
                    apkUrl = parsed.apkAsset.downloadUrl,
                    apkFileName = parsed.apkAsset.name,
                    apkSizeBytes = parsed.apkAsset.sizeBytes,
                    expectedSha256 = expectedHash
                )
            )
        } catch (e: IOException) {
            ReleaseFetchResult.Failure("Brak odpowiedzi z GitHuba (${e.message ?: "błąd sieci"}).", e)
        } catch (e: Exception) {
            ReleaseFetchResult.Failure("Błąd sprawdzania aktualizacji (${e.message ?: "nieznany"}).", e)
        }
    }

    private fun downloadText(url: String, acceptHeader: String): String? {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("Accept", acceptHeader)
            setRequestProperty("User-Agent", userAgent)
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        }
        return try {
            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                return null
            }
            if (code !in 200..299) {
                throw IOException("HTTP $code")
            }
            conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 5_000
        private const val READ_TIMEOUT_MS = 8_000
    }
}
