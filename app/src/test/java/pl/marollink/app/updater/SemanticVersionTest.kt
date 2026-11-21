package pl.marollink.app.updater

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticVersionTest {

    @Test
    fun parsesStandardAndPrefixedTags() {
        assertEquals(SemanticVersion(1, 0, 2), SemanticVersion.parse("v1.0.2"))
        assertEquals(SemanticVersion(1, 2, 3), SemanticVersion.parse("1.2.3"))
        assertEquals(SemanticVersion(1, 0, 0), SemanticVersion.parse("1.0.0-debug"))
    }

    @Test
    fun comparesVersionsNumerically() {
        val v100 = SemanticVersion.parse("1.0.0")!!
        val v101 = SemanticVersion.parse("v1.0.1")!!
        val v120 = SemanticVersion.parse("1.2.0")!!
        val v1100 = SemanticVersion.parse("1.10.0")!!

        assertTrue(v101 > v100)
        assertTrue(v120 > v101)
        assertTrue(v1100 > v120)
        assertEquals(0, v100.compareTo(SemanticVersion.parse("v1.0.0-debug")!!))
    }

    @Test
    fun returnsNullOnInvalidInput() {
        assertNull(SemanticVersion.parse(""))
        assertNull(SemanticVersion.parse("invalid-version"))
    }
}
