package com.moman.domain.category.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.category.model.Category
import com.moman.domain.category.repository.CategoryRepository
import com.moman.domain.library.model.LibrarySort
import com.moman.domain.library.model.plus
import com.moman.domain.library.service.LibraryPreferences
import kotlin.random.Random

@Inject
class SetSortModeForCategory(
    private val preferences: LibraryPreferences,
    private val categoryRepository: CategoryRepository,
) {

    suspend fun await(categoryId: Long?, type: LibrarySort.Type, direction: LibrarySort.Direction) {
        val category = categoryId?.let { categoryRepository.get(it) }
        val flags = (category?.flags ?: 0) + type + direction
        if (type == LibrarySort.Type.Random) {
            preferences.randomSortSeed.set(Random.nextInt())
        }
        if (category != null && preferences.categorizedDisplaySettings.get()) {
            categoryRepository.updateFlags(categoryId = category.id, flags = flags)
        } else {
            preferences.sortingMode.set(LibrarySort(type, direction))
            categoryRepository.updateAllFlags(flags)
        }
    }

    suspend fun await(
        category: Category?,
        type: LibrarySort.Type,
        direction: LibrarySort.Direction,
    ) {
        await(category?.id, type, direction)
    }
}
