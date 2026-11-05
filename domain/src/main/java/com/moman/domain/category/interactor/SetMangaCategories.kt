package com.moman.domain.category.interactor

import dev.zacsweers.metro.Inject
import logcat.LogPriority
import com.moman.core.common.util.system.logcat
import com.moman.domain.manga.repository.MangaRepository

@Inject
class SetMangaCategories(
    private val mangaRepository: MangaRepository,
) {

    suspend fun await(mangaId: Long, categoryIds: List<Long>) {
        try {
            mangaRepository.setMangaCategories(mangaId, categoryIds)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }
}
