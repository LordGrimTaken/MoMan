package com.moman.domain.category.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.category.repository.CategoryRepository
import com.moman.domain.library.model.plus
import com.moman.domain.library.service.LibraryPreferences

@Inject
class ResetCategoryFlags(
    private val preferences: LibraryPreferences,
    private val categoryRepository: CategoryRepository,
) {

    suspend fun await() {
        val sort = preferences.sortingMode.get()
        categoryRepository.updateAllFlags(sort.type + sort.direction)
    }
}
