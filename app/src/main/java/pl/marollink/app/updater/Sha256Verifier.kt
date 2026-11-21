package pl.marollink.app.updater

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object Sha256Verifier {

    fun computeSha256(file: File): String =
        file.inputStream().use { computeSha256(it) }

    fun computeSha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(16 * 1024)
        while (true) {
            val read = inputStream.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    fun verifyFile(file: File, expectedSha256Hex: String): Boolean {
        val normalizedExpected = expectedSha256Hex.trim().lowercase()
        if (normalizedExpected.length != 64 || !file.exists()) return false
        return computeSha256(file).equals(normalizedExpected, ignoreCase = true)
    }
}
