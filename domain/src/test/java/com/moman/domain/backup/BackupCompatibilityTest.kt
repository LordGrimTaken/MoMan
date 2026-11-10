package com.moman.domain.backup

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BackupCompatibilityTest {

    @Test
    fun `legacy backups without a version are restorable`() {
        BackupCompatibility.isRestorable(BackupCompatibility.LEGACY) shouldBe true
    }

    @Test
    fun `same version is restorable`() {
        BackupCompatibility.isRestorable(BackupCompatibility.CURRENT) shouldBe true
    }

    @Test
    fun `older version is restorable`() {
        BackupCompatibility.isRestorable(1, currentVersion = 2) shouldBe true
    }

    @Test
    fun `newer version is refused`() {
        // Reading it would succeed and silently drop the fields this build cannot represent.
        BackupCompatibility.isRestorable(2, currentVersion = 1) shouldBe false
    }
}
