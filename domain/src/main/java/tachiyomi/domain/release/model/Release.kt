package tachiyomi.domain.release.model

/**
 * Contains information about the latest release.
 */
data class Release(
    val version: String,
    val info: String,
    val releaseLink: String,
    val downloadLink: String,
    /** Monotonic build number of the release; 0 when the server did not provide one. */
    val versionCode: Int = 0,
)
