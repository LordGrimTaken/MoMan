package com.moman.domain.release.model

/**
 * Chooses which APK in the release feed a device should download.
 *
 * The feed lists artefacts by ABI plus an optional `universal` fallback. Picking wrongly is not a cosmetic failure:
 * the download succeeds, then Android's installer rejects it (`INSTALL_FAILED_NO_MATCHING_ABIS`), which reads to the
 * user as "the update is broken". Kept free of Android types so the order of preference is unit-testable.
 */
object ReleaseApkSelector {
    /** @param available abi -> download URL, as served by `latest.json` */
    fun pick(available: Map<String, String>, supportedAbis: List<String>): String? {
        // Device ABIs are already ordered by preference; take the first one the feed actually offers.
        supportedAbis.firstOrNull { available.containsKey(it) }?.let { return available[it] }
        return available["universal"]
    }
}
