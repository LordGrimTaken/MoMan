package com.moman.domain.release.interactor

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.moman.domain.release.model.Release
import com.moman.domain.release.service.ReleaseService

class GetApplicationReleaseTest {

    private lateinit var getApplicationRelease: GetApplicationRelease
    private lateinit var releaseService: ReleaseService

    @BeforeEach
    fun beforeEach() {
        releaseService = mockk()

        getApplicationRelease = GetApplicationRelease(releaseService)
    }

    @Test
    fun `When has update but is nightly expect new update`() = runTest {
        val release = Release(
            "r2000",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = true,
                commitCount = 1000,
                versionName = "",
                repository = "test",
            ),
        )

        (result as GetApplicationRelease.Result.NewUpdate).release shouldBe GetApplicationRelease.Result.NewUpdate(
            release,
        ).release
    }

    @Test
    fun `When has update expect new update`() = runTest {
        val release = Release(
            "v2.0.0",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "v1.0.0",
                repository = "test",
            ),
        )

        (result as GetApplicationRelease.Result.NewUpdate).release shouldBe GetApplicationRelease.Result.NewUpdate(
            release,
        ).release
    }

    @Test
    fun `When has no update expect no new update`() = runTest {
        val release = Release(
            "v1.0.0",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "v2.0.0",
                repository = "test",
            ),
        )

        result shouldBe GetApplicationRelease.Result.NoNewUpdate
    }

    @Test
    fun `When release versionCode is newer expect new update`() = runTest {
        // Same display name as the running build, newer code: this is the case the name comparison missed.
        val release = Release(
            "1.0.12",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
            versionCode = 14,
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "1.0.12",
                repository = "test",
                versionCode = 13,
            ),
        )

        (result as GetApplicationRelease.Result.NewUpdate).release.versionCode shouldBe 14
    }

    @Test
    fun `When release versionCode matches expect no update despite name suffix`() = runTest {
        // Debug builds carry names like "1.0.12-36"; a name comparison would either miss or fake an update here.
        val release = Release(
            "1.0.12",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
            versionCode = 13,
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "1.0.12-36",
                repository = "test",
                versionCode = 13,
            ),
        )

        result shouldBe GetApplicationRelease.Result.NoNewUpdate
    }

    @Test
    fun `When release versionCode is older expect no update`() = runTest {
        val release = Release(
            "1.0.11",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
            versionCode = 12,
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "1.0.12",
                repository = "test",
                versionCode = 13,
            ),
        )

        result shouldBe GetApplicationRelease.Result.NoNewUpdate
    }

    @Test
    fun `When running build is below the minimum supported code the update is required`() = runTest {
        val release = Release(
            "1.0.12",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
            versionCode = 13,
            minSupportedVersionCode = 13,
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "1.0.11",
                repository = "test",
                versionCode = 12,
            ),
        )

        val update = result as GetApplicationRelease.Result.NewUpdate
        update.required shouldBe true
    }

    @Test
    fun `When running build meets the minimum the update is optional`() = runTest {
        val release = Release(
            "1.0.13",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
            versionCode = 14,
            minSupportedVersionCode = 13,
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "1.0.12",
                repository = "test",
                versionCode = 13,
            ),
        )

        (result as GetApplicationRelease.Result.NewUpdate).required shouldBe false
    }

    @Test
    fun `A required update is offered even when the versions look current`() = runTest {
        // Same versionName and same versionCode as the feed, but the feed has raised its floor above this build.
        val release = Release(
            "1.0.12",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
            versionCode = 13,
            minSupportedVersionCode = 14,
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "1.0.12",
                repository = "test",
                versionCode = 13,
            ),
        )

        val update = result as GetApplicationRelease.Result.NewUpdate
        update.required shouldBe true
    }
}
