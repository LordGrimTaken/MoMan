package com.moman.domain.release.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TransferProgressTest {

    @Test
    fun `half way is fifty percent`() {
        TransferProgress.percent(50, 100) shouldBe 50
    }

    @Test
    fun `complete is one hundred`() {
        TransferProgress.percent(100, 100) shouldBe 100
    }

    @Test
    fun `overshoot is clamped`() {
        TransferProgress.percent(120, 100) shouldBe 100
    }

    @Test
    fun `unknown length yields null instead of a negative percentage`() {
        // HTTP chunked responses report -1
        TransferProgress.percent(10, -1) shouldBe null
        TransferProgress.percent(0, 0) shouldBe null
    }

    @Test
    fun `negative bytes read yields null`() {
        TransferProgress.percent(-5, 100) shouldBe null
    }

    @Test
    fun `first tick emits`() {
        // Wall-clock milliseconds: the listener starts with lastTick = 0, so the first tick clears the window.
        TransferProgress.shouldEmit(previousPercent = 0, previousAtMs = 0, percent = 1, nowMs = 1_000_000) shouldBe true
    }

    @Test
    fun `tick inside the throttle window is dropped`() {
        TransferProgress.shouldEmit(previousPercent = 10, previousAtMs = 1_000_000, percent = 12, nowMs = 1_000_100) shouldBe false
    }

    @Test
    fun `tick after the window emits`() {
        TransferProgress.shouldEmit(previousPercent = 10, previousAtMs = 1_000_000, percent = 12, nowMs = 1_000_300) shouldBe true
    }

    @Test
    fun `completion always emits, even inside the window`() {
        // This is the case the old inline condition dropped: the bar stayed one percent short.
        TransferProgress.shouldEmit(previousPercent = 99, previousAtMs = 1_000_000, percent = 100, nowMs = 1_000_050, done = true) shouldBe true
    }

    @Test
    fun `repeated identical progress does not emit`() {
        TransferProgress.shouldEmit(previousPercent = 40, previousAtMs = 1_000_000, percent = 40, nowMs = 1_005_000) shouldBe false
    }
}
