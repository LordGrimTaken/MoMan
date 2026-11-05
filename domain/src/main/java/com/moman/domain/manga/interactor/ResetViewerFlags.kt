package com.moman.domain.manga.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.manga.repository.MangaRepository

@Inject
class ResetViewerFlags(
    private val mangaRepository: MangaRepository,
) {

    suspend fun await(): Boolean {
        return mangaRepository.resetViewerFlags()
    }
}
