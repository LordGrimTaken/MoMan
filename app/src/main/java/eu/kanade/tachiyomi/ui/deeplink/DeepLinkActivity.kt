package eu.kanade.tachiyomi.ui.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import eu.kanade.tachiyomi.ui.main.MainActivity

class DeepLinkActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Exported component: only pass on the actions this activity declares in the manifest. Anything else —
        // notably ACTION_VIEW, which MainActivity uses for repo links and backup restores — would let an untrusted
        // caller reach flows that were never exposed here.
        if (!DeepLinkActions.isForwardable(intent?.action)) {
            finish()
            return
        }

        intent.apply {
            flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            setClass(applicationContext, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
