package com.moman.domain.chapter.interactor

import dev.zacsweers.metro.Inject
import logcat.LogPriority
import com.moman.core.common.util.system.logcat
import com.moman.domain.chapter.model.Chapter
import com.moman.domain.chapter.repository.ChapterRepository

@Inject
class GetBookmarkedChaptersByMangaId(
    private val chapterRepository: ChapterRepository,
) {

    suspend fun await(mangaId: Long): List<Chapter> {
        return try {
            chapterRepository.getBookmarkedChaptersByMangaId(mangaId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }
}
