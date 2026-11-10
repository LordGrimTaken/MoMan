package com.moman.domain.release.model

/**
 * Identity rules for an APK downloaded by the self-updater.
 *
 * The updater fetches an APK from the project's own server and hands it to Android's installer. Before that
 * happens the file must be *the same app*: same package id and signed by the same certificate as the running
 * build. Otherwise a swapped or tampered file would be offered to the user as an update (Android would refuse to
 * install it, but with a confusing error long after the download).
 *
 * Kept free of Android types so the rule is unit-testable; callers pass certificate fingerprints as strings.
 */
object ApkIdentity {
    /**
     * @param expectedPackage package id of the running app
     * @param actualPackage package id read from the downloaded APK, or null if it could not be parsed
     * @param expectedCerts SHA-256 fingerprints of the running app's signing certificates
     * @param actualCerts SHA-256 fingerprints of the downloaded APK's signing certificates
     */
    fun isTrustedUpdate(
        expectedPackage: String,
        actualPackage: String?,
        expectedCerts: Set<String>,
        actualCerts: Set<String>,
    ): Boolean {
        if (actualPackage != expectedPackage) return false
        // An unsigned or unreadable APK is never trusted, and never accept an empty expectation.
        if (expectedCerts.isEmpty() || actualCerts.isEmpty()) return false
        return actualCerts == expectedCerts
    }
}
