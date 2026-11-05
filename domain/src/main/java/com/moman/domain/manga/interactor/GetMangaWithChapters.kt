package com.moman.domain.manga.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.moman.domain.chapter.model.Chapter
import com.moman.domain.chapter.repository.ChapterRepository
import com.moman.domain.manga.model.Manga
import com.moman.domain.manga.repository.MangaRepository

@Inject
class GetMangaWithChapters(
    private val mangaRepository: MangaRepository,
    private val chapterRepository: ChapterRepository,
) {

    suspend fun subscribe(id: Long, applyScanlatorFilter: Boolean = false): Flow<Pair<Manga, List<Chapter>>> {
        return combine(
            mangaRepository.getMangaByIdAsFlow(id),
            chapterRepository.getChapterByMangaIdAsFlow(id, applyScanlatorFilter),
        ) { manga, chapters ->
            Pair(manga, chapters)
        }
    }

    suspend fun awaitManga(id: Long): Manga {
        return mangaRepository.getMangaById(id)
    }

    suspend fun awaitChapters(id: Long, applyScanlatorFilter: Boolean = false): List<Chapter> {
        return chapterRepository.getChapterByMangaId(id, applyScanlatorFilter)
    }
}
