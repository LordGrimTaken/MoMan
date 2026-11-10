package com.moman.domain.release.model

/**
 * Progress arithmetic for the self-updater's download.
 *
 * The updater showed `100 * bytesRead / contentLength` straight from the HTTP layer. A response without a
 * `Content-Length` reports -1, which made that expression negative (or a division by zero on an empty body), so the
 * UI could sit at a nonsense percentage for the whole download.
 */
object TransferProgress {
    /**
     * @return 0..100, or null when the total size is unknown (-1 or 0) and a percentage would be meaningless.
     */
    fun percent(bytesRead: Long, contentLength: Long): Int? {
        if (contentLength <= 0L || bytesRead < 0L) return null
        val raw = (100 * bytesRead.toDouble() / contentLength.toDouble())
        return raw.toInt().coerceIn(0, 100)
    }

    /**
     * Whether a progress tick is worth emitting.
     *
     * The updater used `progress > saved && now - lastTick > 200`, which silently drops the tick that reaches 100%
     * when it lands inside the throttle window — the bar then sits a percent short until the stage changes. A
     * completed transfer always emits; otherwise emit only on real progress, at most once per [intervalMs].
     */
    fun shouldEmit(
        previousPercent: Int,
        previousAtMs: Long,
        percent: Int,
        nowMs: Long,
        done: Boolean = false,
        intervalMs: Long = 200,
    ): Boolean {
        if (done) return true
        if (percent <= previousPercent) return false
        return nowMs - previousAtMs >= intervalMs
    }
}
