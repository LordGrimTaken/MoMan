package eu.kanade.tachiyomi.util.system

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Soft "stamp" haptic tick, e.g. when a chapter is finished. Always on, ignores the user's
 * touch-feedback setting only as far as the platform allows (FLAG_IGNORE_GLOBAL_SETTING is not used).
 */
fun View.stampHaptic() {
    val constant = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> HapticFeedbackConstants.CONTEXT_CLICK
        else -> HapticFeedbackConstants.VIRTUAL_KEY
    }
    performHapticFeedback(constant)
}

/** Convenience for an Activity context: performs [stampHaptic] on its decor view. */
fun Context.performSealHaptic() {
    val activity = generateSequence(this) { (it as? android.content.ContextWrapper)?.baseContext }
        .filterIsInstance<android.app.Activity>()
        .firstOrNull()
    activity?.window?.decorView?.stampHaptic()
}
