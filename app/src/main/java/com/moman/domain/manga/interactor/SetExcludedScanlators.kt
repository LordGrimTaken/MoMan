package com.moman.domain.manga.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.manga.repository.MangaRepository

@Inject
class SetExcludedScanlators(
    private val mangaRepository: MangaRepository,
) {

    suspend fun await(mangaId: Long, excludedScanlators: Set<String>) {
        mangaRepository.setExcludedScanlators(mangaId, excludedScanlators)
    }
}
