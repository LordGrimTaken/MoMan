package com.moman.domain.release.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ReleaseApkSelectorTest {

    @Test
    fun `prefers the device's primary abi`() {
        val feed = mapOf("arm64-v8a" to "a64", "armeabi-v7a" to "a32", "universal" to "uni")
        ReleaseApkSelector.pick(feed, listOf("arm64-v8a", "armeabi-v7a")) shouldBe "a64"
    }

    @Test
    fun `falls back to the second abi the feed offers`() {
        val feed = mapOf("armeabi-v7a" to "a32", "universal" to "uni")
        ReleaseApkSelector.pick(feed, listOf("arm64-v8a", "armeabi-v7a")) shouldBe "a32"
    }

    @Test
    fun `uses universal when no abi matches`() {
        val feed = mapOf("x86_64" to "x64", "universal" to "uni")
        ReleaseApkSelector.pick(feed, listOf("arm64-v8a")) shouldBe "uni"
    }

    @Test
    fun `returns null when nothing is installable`() {
        ReleaseApkSelector.pick(mapOf("x86_64" to "x64"), listOf("arm64-v8a")) shouldBe null
        ReleaseApkSelector.pick(emptyMap(), listOf("arm64-v8a")) shouldBe null
    }
}
