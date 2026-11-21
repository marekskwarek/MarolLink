package pl.marollink.app.updater

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubReleaseParserTest {

    @Test
    fun parsesReleaseWithApkAndSha256Asset() {
        val json = """
            {
              "tag_name": "v1.2.0",
              "name": "MarolLink v1.2.0",
              "body": "- Poprawki interfejsu\n- Stabilniejsze OTA",
              "draft": false,
              "assets": [
                {
                  "name": "MarolLink-v1.2.0.apk",
                  "browser_download_url": "https://github.com/marekskwarek/MarolLink/releases/download/v1.2.0/MarolLink-v1.2.0.apk",
                  "size": 12345678
                },
                {
                  "name": "MarolLink-v1.2.0.apk.sha256",
                  "browser_download_url": "https://github.com/marekskwarek/MarolLink/releases/download/v1.2.0/MarolLink-v1.2.0.apk.sha256",
                  "size": 92
                }
              ]
            }
        """.trimIndent()

        val parsed = GitHubReleaseParser.parseReleaseJson(json)
        assertNotNull(parsed)
        assertEquals("v1.2.0", parsed!!.tagName)
        assertEquals(SemanticVersion(1, 2, 0), parsed.version)
        assertEquals("MarolLink-v1.2.0.apk", parsed.apkAsset.name)
        assertEquals("MarolLink-v1.2.0.apk.sha256", parsed.sha256Asset?.name)
    }

    @Test
    fun ignoresDraftReleases() {
        val draftJson = """
            {
              "tag_name": "v1.2.1",
              "draft": true,
              "assets": [
                { "name": "MarolLink-v1.2.1.apk", "browser_download_url": "https://example.com/a.apk", "size": 100 }
              ]
            }
        """.trimIndent()

        assertNull(GitHubReleaseParser.parseReleaseJson(draftJson))
    }

    @Test
    fun parsesSha256SumFormat() {
        val expected = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        val content = "$expected  MarolLink-v1.2.0.apk\n"
        assertEquals(
            expected,
            GitHubReleaseParser.parseSha256Content(content, "MarolLink-v1.2.0.apk")
        )
    }
}
