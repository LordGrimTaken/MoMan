package com.moman.data.release

import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import com.moman.domain.release.interactor.GetApplicationRelease
import com.moman.domain.release.model.Release
import com.moman.domain.release.service.ReleaseService

/**
 * Fetches the latest MoMan release from the MoMan update server.
 * [GetApplicationRelease.Arguments.repository] holds the server's base URL.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ReleaseServiceImpl(
    private val networkService: NetworkHelper,
    private val json: Json,
) : ReleaseService {

    override suspend fun latest(arguments: GetApplicationRelease.Arguments): Release? {
        val baseUrl = arguments.repository.trimEnd('/')
        val release = withContext(Dispatchers.IO) {
            with(json) {
                networkService.client
                    .newCall(GET("$baseUrl/latest.json"))
                    .awaitSuccess()
                    .parseAs<MomanRelease>()
            }
        }

        val downloadLink = pickApk(release) ?: return null

        return Release(
            version = release.version,
            info = release.changelog,
            releaseLink = baseUrl,
            downloadLink = downloadLink,
        )
    }

    /** Prefer the APK built for this device's primary ABI, then fall back to the universal build. */
    private fun pickApk(release: MomanRelease): String? {
        Build.SUPPORTED_ABIS.forEach { abi -> release.apks[abi]?.let { return it } }
        return release.apks["universal"]
    }
}
