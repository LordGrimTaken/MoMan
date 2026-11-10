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
)
