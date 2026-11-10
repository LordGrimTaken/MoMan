package eu.kanade.tachiyomi.ui.deeplink

import android.content.Intent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DeepLinkActionsTest {

    @Test
    fun `declared manifest actions are forwardable`() {
        DeepLinkActions.isForwardable(Intent.ACTION_SEARCH) shouldBe true
        DeepLinkActions.isForwardable(DeepLinkActions.GMS_SEARCH_ACTION) shouldBe true
        DeepLinkActions.isForwardable(DeepLinkActions.MOMAN_SEARCH_ACTION) shouldBe true
        DeepLinkActions.isForwardable(Intent.ACTION_SEND) shouldBe true
        DeepLinkActions.isForwardable(Intent.ACTION_APPLICATION_PREFERENCES) shouldBe true
    }

    @Test
    fun `view is not forwardable from the exported search activity`() {
        // MainActivity handles ACTION_VIEW for repo links and .tachibk restores; the search activity must not be a
        // side door into those flows.
        DeepLinkActions.isForwardable(Intent.ACTION_VIEW) shouldBe false
    }

    @Test
    fun `unknown and missing actions are not forwardable`() {
        DeepLinkActions.isForwardable("com.example.SOMETHING") shouldBe false
        DeepLinkActions.isForwardable(null) shouldBe false
        DeepLinkActions.isForwardable("") shouldBe false
    }
}
