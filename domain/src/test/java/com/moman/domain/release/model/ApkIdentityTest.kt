package com.moman.domain.release.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ApkIdentityTest {

    private val pkg = "com.moman.reader"
    private val cert = "1d85bc09bd1f518b4ff734160cdd1fef9e6e120c9dafbb413bff481d22298227"

    @Test
    fun `matching package and certificate is trusted`() {
        ApkIdentity.isTrustedUpdate(pkg, pkg, setOf(cert), setOf(cert)) shouldBe true
    }

    @Test
    fun `different package is rejected`() {
        ApkIdentity.isTrustedUpdate(pkg, "com.example.other", setOf(cert), setOf(cert)) shouldBe false
    }

    @Test
    fun `unreadable package is rejected`() {
        ApkIdentity.isTrustedUpdate(pkg, null, setOf(cert), setOf(cert)) shouldBe false
    }

    @Test
    fun `different signing certificate is rejected`() {
        ApkIdentity.isTrustedUpdate(pkg, pkg, setOf(cert), setOf("deadbeef")) shouldBe false
    }

    @Test
    fun `missing certificates are rejected`() {
        ApkIdentity.isTrustedUpdate(pkg, pkg, emptySet(), setOf(cert)) shouldBe false
        ApkIdentity.isTrustedUpdate(pkg, pkg, setOf(cert), emptySet()) shouldBe false
    }
}
