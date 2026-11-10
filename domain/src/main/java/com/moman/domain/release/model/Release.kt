package com.moman.domain.release.model

/**
 * Contains information about the latest release.
 */
data class Release(
    val version: String,
    val info: String,
    val releaseLink: String,
    val downloadLink: String,
    /** Monotonic version code from the release feed; 0 when the feed predates it. */
    val versionCode: Int = 0,
    /** Oldest version code the feed still supports; 0 when the feed does not say. */
    val minSupportedVersionCode: Int = 0,
)
