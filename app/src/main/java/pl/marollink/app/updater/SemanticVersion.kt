package pl.marollink.app.updater

/**
 * Semantic version (MAJOR.MINOR.PATCH) used to compare installed MarolLink version
 * against GitHub Releases tags (e.g. "v1.0.1" or "1.0.1").
 */
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<SemanticVersion> {

    override fun compareTo(other: SemanticVersion): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        return patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        private val VERSION_REGEX = Regex("""^v?(\d+)(?:\.(\d+))?(?:\.(\d+))?.*$""")

        fun parse(raw: String): SemanticVersion? {
            val cleaned = raw.trim()
                .removeSuffix("-debug")
                .substringBefore('+')
            if (cleaned.isEmpty()) return null
            val match = VERSION_REGEX.matchEntire(cleaned) ?: return null
            val major = match.groupValues[1].toIntOrNull() ?: return null
            val minor = match.groupValues[2].toIntOrNull() ?: 0
            val patch = match.groupValues[3].toIntOrNull() ?: 0
            return SemanticVersion(major, minor, patch)
        }
    }
}
