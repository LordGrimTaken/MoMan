package com.moman.domain.manga.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import com.moman.domain.manga.model.Manga
import com.moman.domain.manga.repository.MangaRepository

@Inject
class GetFavorites(
    private val mangaRepository: MangaRepository,
) {

    suspend fun await(): List<Manga> {
        return mangaRepository.getFavorites()
    }

    fun subscribe(sourceId: Long): Flow<List<Manga>> {
        return mangaRepository.getFavoritesBySourceId(sourceId)
    }
}
