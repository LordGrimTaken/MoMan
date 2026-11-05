package com.moman.domain.chapter.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.chapter.model.ChapterSummary
import com.moman.domain.chapter.repository.ChapterRepository

@Inject
class GetChapterSummaryByMangaId(
    private val chapterRepository: ChapterRepository,
) {

    suspend fun await(mangaId: Long): ChapterSummary {
        return chapterRepository.getChapterSummaryByMangaId(mangaId)
    }
}
