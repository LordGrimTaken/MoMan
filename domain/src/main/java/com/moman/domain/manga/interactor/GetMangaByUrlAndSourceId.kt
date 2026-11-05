package com.moman.domain.manga.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.manga.model.Manga
import com.moman.domain.manga.repository.MangaRepository

@Inject
class GetMangaByUrlAndSourceId(
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(url: String, sourceId: Long): Manga? {
        return mangaRepository.getMangaByUrlAndSourceId(url, sourceId)
    }
}
