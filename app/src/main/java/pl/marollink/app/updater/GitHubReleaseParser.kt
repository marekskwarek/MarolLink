package pl.marollink.app.updater

import org.json.JSONObject

data class GitHubReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

data class ParsedGitHubRelease(
    val tagName: String,
    val version: SemanticVersion,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkAsset: GitHubReleaseAsset,
    val sha256Asset: GitHubReleaseAsset?
)

object GitHubReleaseParser {

    private val SHA256_LINE_REGEX = Regex("""([a-fA-F0-9]{64})""")

    fun parseReleaseJson(rawJson: String): ParsedGitHubRelease? {
        val root = runCatching { JSONObject(rawJson) }.getOrNull() ?: return null
        if (root.optBoolean("draft", false)) return null

        val tagName = root.optString("tag_name", "").trim()
        val version = SemanticVersion.parse(tagName) ?: return null
        val releaseTitle = root.optString("name", tagName).ifBlank { tagName }
        val releaseNotes = root.optString("body", "").trim()

        val assetsJson = root.optJSONArray("assets") ?: return null
        val assets = buildList {
            for (i in 0 until assetsJson.length()) {
                val obj = assetsJson.optJSONObject(i) ?: continue
                val name = obj.optString("name", "").trim()
                val url = obj.optString("browser_download_url", "").trim()
                val size = obj.optLong("size", 0L)
                if (name.isNotEmpty() && url.isNotEmpty()) {
                    add(GitHubReleaseAsset(name = name, downloadUrl = url, sizeBytes = size))
                }
            }
        }

        val apkAsset = assets.firstOrNull { asset ->
            asset.name.endsWith(".apk", ignoreCase = true) &&
                !asset.name.contains("debug", ignoreCase = true)
        } ?: assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            ?: return null

        val matchingSha = assets.firstOrNull {
            it.name.equals("${apkAsset.name}.sha256", ignoreCase = true)
        } ?: assets.firstOrNull {
            it.name.endsWith(".sha256", ignoreCase = true)
        }

        return ParsedGitHubRelease(
            tagName = tagName,
            version = version,
            releaseTitle = releaseTitle,
            releaseNotes = releaseNotes,
            apkAsset = apkAsset,
            sha256Asset = matchingSha
        )
    }

    /**
     * Parses GNU sha256sum output ("<64 hex chars>  filename.apk") or a plain 64-char hash string.
     */
    fun parseSha256Content(content: String, expectedFileName: String? = null): String? {
        val lines = content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.isEmpty()) return null

        if (!expectedFileName.isNullOrBlank()) {
            val matchingLine = lines.firstOrNull { line ->
                line.contains(expectedFileName, ignoreCase = true)
            }
            if (matchingLine != null) {
                return SHA256_LINE_REGEX.find(matchingLine)?.groupValues?.get(1)?.lowercase()
            }
        }

        return lines.firstNotNullOfOrNull { line ->
            SHA256_LINE_REGEX.find(line)?.groupValues?.get(1)?.lowercase()
        }
    }
}
