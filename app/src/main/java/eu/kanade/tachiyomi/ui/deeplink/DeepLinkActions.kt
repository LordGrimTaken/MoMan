package eu.kanade.tachiyomi.ui.deeplink

import android.content.Intent

/**
 * The actions [DeepLinkActivity] is allowed to forward to [eu.kanade.tachiyomi.ui.main.MainActivity].
 *
 * The activity is exported (search, share, app-settings shortcuts), so any app on the device can start it with an
 * arbitrary action. Forwarding blindly meant an untrusted caller could reach MainActivity's `ACTION_VIEW` handling —
 * which installs an extension repository from a `?url=` parameter and restores `.tachibk` backups — without ever
 * declaring that action on this component. Only the actions its own manifest filters advertise are forwarded.
 */
object DeepLinkActions {
    const val GMS_SEARCH_ACTION = "com.google.android.gms.actions.SEARCH_ACTION"
    const val MOMAN_SEARCH_ACTION = "eu.kanade.tachiyomi.SEARCH"

    private val forwardable = setOf(
        Intent.ACTION_SEARCH,
        GMS_SEARCH_ACTION,
        MOMAN_SEARCH_ACTION,
        Intent.ACTION_SEND,
        Intent.ACTION_APPLICATION_PREFERENCES,
    )

    fun isForwardable(action: String?): Boolean = action != null && action in forwardable
}
