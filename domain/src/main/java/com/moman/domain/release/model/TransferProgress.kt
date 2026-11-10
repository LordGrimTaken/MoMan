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
}
