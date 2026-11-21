package pl.marollink.app.updater

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class Sha256VerifierTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun verifiesValidFileAndRejectsCorruptedHash() {
        val file = tempFolder.newFile("sample.apk")
        file.writeBytes("MarolLink OTA payload".toByteArray(Charsets.UTF_8))

        val computed = Sha256Verifier.computeSha256(file)
        assertEquals(64, computed.length)
        assertTrue(Sha256Verifier.verifyFile(file, computed.uppercase()))

        val tampered = "f".repeat(64)
        assertFalse(Sha256Verifier.verifyFile(file, tampered))
        assertFalse(Sha256Verifier.verifyFile(file, "too-short"))
    }
}
