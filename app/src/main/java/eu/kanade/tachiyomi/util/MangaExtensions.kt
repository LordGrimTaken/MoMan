package eu.kanade.tachiyomi.util

import com.moman.domain.manga.interactor.UpdateManga
import com.moman.domain.manga.model.toSManga
import eu.kanade.tachiyomi.data.cache.CoverCache
import com.moman.domain.manga.model.Manga
import com.moman.source.local.image.LocalCoverManager
import com.moman.source.local.isLocal
import java.io.InputStream

fun Manga.removeCovers(coverCache: CoverCache) {
    if (isLocal()) return
    coverCache.deleteFromCache(this, true)
}

suspend fun Manga.editCover(
    coverManager: LocalCoverManager,
    stream: InputStream,
    updateManga: UpdateManga,
    coverCache: CoverCache,
) {
    if (isLocal()) {
        coverManager.update(toSManga(), stream)
        updateManga.awaitUpdateCoverLastModified(id)
    } else if (favorite) {
        coverCache.setCustomCoverToCache(this, stream)
        updateManga.awaitUpdateCoverLastModified(id)
    }
}
