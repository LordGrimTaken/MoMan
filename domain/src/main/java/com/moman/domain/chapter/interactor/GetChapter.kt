package com.moman.domain.chapter.interactor

import dev.zacsweers.metro.Inject
import logcat.LogPriority
import com.moman.core.common.util.system.logcat
import com.moman.domain.chapter.model.Chapter
import com.moman.domain.chapter.repository.ChapterRepository

@Inject
class GetChapter(
    private val chapterRepository: ChapterRepository,
) {

    suspend fun await(id: Long): Chapter? {
        return try {
            chapterRepository.getChapterById(id)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            null
        }
    }

    suspend fun await(url: String, mangaId: Long): Chapter? {
        return try {
            chapterRepository.getChapterByUrlAndMangaId(url, mangaId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            null
        }
    }
}
