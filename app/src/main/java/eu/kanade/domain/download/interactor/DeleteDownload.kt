package eu.kanade.domain.download.interactor

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.download.DownloadManager
import com.moman.domain.chapter.model.Chapter
import com.moman.domain.manga.model.Manga
import com.moman.domain.source.service.SourceManager

@Inject
class DeleteDownload(
    private val sourceManager: SourceManager,
    private val downloadManager: DownloadManager,
) {

    suspend fun awaitAll(manga: Manga, vararg chapters: Chapter) {
        sourceManager.get(manga.source)?.let { source ->
            downloadManager.deleteChapters(chapters.toList(), manga, source)
        }
    }
}
