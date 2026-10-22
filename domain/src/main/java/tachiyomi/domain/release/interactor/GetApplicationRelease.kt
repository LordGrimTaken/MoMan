package tachiyomi.domain.release.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.ReleaseService

@Inject
class GetApplicationRelease(
    private val service: ReleaseService,
) {
    suspend fun await(arguments: Arguments): Result {
        val release = service.latest(arguments) ?: return Result.NoNewUpdate

        val isNewVersion = isNewVersion(
            currentVersionCode = arguments.versionCode,
            currentVersionName = arguments.versionName,
            release = release,
        )
        if (!isNewVersion) return Result.NoNewUpdate

        check(release.downloadLink.isNotBlank()) {
            "Version ${release.version} is available but has no download for this device"
        }
        return Result.NewUpdate(release)
    }

    private fun isNewVersion(
        currentVersionCode: Int,
        currentVersionName: String,
        release: Release,
    ): Boolean {
        // Preferred rule: the server's version code is authoritative (works for debug builds too).
        if (release.versionCode > 0) {
            return release.versionCode > currentVersionCode
        }

        // Fallback: semver comparison, ignoring any "-suffix" (e.g. "-152" on debug builds).
        val newSemVer = parseSemVer(release.version)
        val oldSemVer = parseSemVer(currentVersionName)
        val size = maxOf(newSemVer.size, oldSemVer.size)
        for (i in 0 until size) {
            val n = newSemVer.getOrElse(i) { 0 }
            val o = oldSemVer.getOrElse(i) { 0 }
            if (n != o) return n > o
        }
        return false
    }

    private fun parseSemVer(version: String): List<Int> {
        // Strip "-suffix", then prefixes like "v"
        val core = version.substringBefore('-').replace("[^\\d.]".toRegex(), "")
        return core.split(".").mapNotNull { it.toIntOrNull() }
    }

    data class Arguments(
        val isFoss: Boolean,
        val versionCode: Int,
        val versionName: String,
        val repository: String,
        val forceCheck: Boolean = false,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}
