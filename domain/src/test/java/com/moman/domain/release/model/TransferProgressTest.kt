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
}
