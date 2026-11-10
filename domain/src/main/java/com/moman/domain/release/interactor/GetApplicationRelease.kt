package com.moman.domain.release.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.release.model.Release
import com.moman.domain.release.service.ReleaseService

@Inject
class GetApplicationRelease(
    private val service: ReleaseService,
) {
    suspend fun await(arguments: Arguments): Result {
        val release = service.latest(arguments) ?: return Result.NoNewUpdate

        // A build below the feed's minimum is told to update even when the versions otherwise look equal:
        // that is what stops an old client from staying on a version the server no longer supports.
        val required = release.minSupportedVersionCode > 0 &&
            arguments.versionCode > 0 &&
            arguments.versionCode < release.minSupportedVersionCode
        return if (required || isNewVersion(arguments, release)) {
            Result.NewUpdate(release, required)
        } else {
            Result.NoNewUpdate
        }
    }

    /**
     * Decides whether [release] is newer than the running build.
     *
     * The feed's `versionCode` is authoritative when present: version *names* are display strings that can carry
     * suffixes (`1.0.12-36` on debug builds) or drift between builds of identical code, so comparing them can both
     * miss real updates and offer ones that cannot be installed. Feeds that predate `versionCode` fall back to the
     * old name comparison so an old server keeps working.
     */
    private fun isNewVersion(arguments: Arguments, release: Release): Boolean {
        if (release.versionCode > 0) {
            return release.versionCode > arguments.versionCode
        }
        return isNewVersionByName(arguments, release.version)
    }

    private fun isNewVersionByName(arguments: Arguments, versionTag: String): Boolean {
        // Removes prefixes like "r" or "v"
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")
        if (arguments.isNightly) {
            return newVersion.toIntOrNull()?.let { it > arguments.commitCount } ?: false
        }

        val newSemVer = newVersion.split(".").mapNotNull { it.toIntOrNull() }
        val oldSemVer = arguments.versionName.replace("[^\\d.]".toRegex(), "")
            .split(".")
            .mapNotNull { it.toIntOrNull() }

        // Compare component-wise; missing components count as 0 so "1.1" and "1.1.0" agree and a shorter
        // incoming version can never throw.
        for (index in 0 until maxOf(newSemVer.size, oldSemVer.size)) {
            val newPart = newSemVer.getOrNull(index) ?: 0
            val oldPart = oldSemVer.getOrNull(index) ?: 0
            if (newPart != oldPart) return newPart > oldPart
        }
        return false
    }

    data class Arguments(
        val isFoss: Boolean,
        val isNightly: Boolean,
        val commitCount: Int,
        val versionName: String,
        val repository: String,
        val forceCheck: Boolean = false,
        /** The running build's versionCode; 0 disables the code comparison and forces name comparison. */
        val versionCode: Int = 0,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release, val required: Boolean = false) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}
