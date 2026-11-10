package com.moman.domain.backup

/**
 * Whether a backup file can be restored by this build.
 *
 * Backups are protobuf: a field this build does not know is silently ignored. That makes restoring a backup written by
 * a *newer* app look successful while quietly discarding whatever it cannot represent. Backups written before the
 * version field existed report [LEGACY] and are accepted, because an unknown format is the status quo ante.
 */
object BackupCompatibility {
    /** Current format. Bump when a change would be lossy to read with an older build. */
    const val CURRENT: Int = 1

    /** Backups written before the field existed. */
    const val LEGACY: Int = 0

    fun isRestorable(storedVersion: Int, currentVersion: Int = CURRENT): Boolean =
        storedVersion <= currentVersion
}
