package eu.kanade.tachiyomi.data.updater

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.util.system.isFossBuildType
import eu.kanade.tachiyomi.util.system.isNightlyBuildType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.moman.domain.release.interactor.GetApplicationRelease

@Inject
class AppUpdateChecker(
    private val getApplicationRelease: GetApplicationRelease,
) {

    suspend fun checkForUpdate(forceCheck: Boolean = false): GetApplicationRelease.Result {
        // Disable app update checks for older Android versions that we're going to drop support for
        // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        //     return GetApplicationRelease.Result.OsTooOld
        // }

        return withContext(Dispatchers.IO) {
            val result = getApplicationRelease.await(
                GetApplicationRelease.Arguments(
                    isFossBuildType,
                    isNightlyBuildType,
                    BuildConfig.COMMIT_COUNT.toInt(),
                    BuildConfig.VERSION_NAME,
                    UPDATE_BASE_URL,
                    forceCheck,
                    versionCode = BuildConfig.VERSION_CODE,
                ),
            )

            result
        }
    }
}

/**
 * Base URL of the MoMan update server.
 *
 * Served by the Cloudflare Worker in `cloudflare/worker.js` from the `moman-site` R2 bucket, which answers
 * `/latest.json` (see `server/releases.json`) and `/apk/<file>`. The legacy Render host
 * (`moman-updates.onrender.com`) is decommissioned and returns 503 — do not point this back at it.
 */
const val UPDATE_BASE_URL = "https://momanreader.com"

val RELEASE_URL = UPDATE_BASE_URL
