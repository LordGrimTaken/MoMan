package com.moman.domain.category.interactor

import dev.zacsweers.metro.Inject
import logcat.LogPriority
import com.moman.core.common.util.system.logcat
import com.moman.domain.category.model.Category
import com.moman.domain.category.repository.CategoryRepository
import com.moman.domain.download.service.DownloadPreferences
import com.moman.domain.library.service.LibraryPreferences

@Inject
class DeleteCategory(
    private val categoryRepository: CategoryRepository,
    private val libraryPreferences: LibraryPreferences,
    private val downloadPreferences: DownloadPreferences,
) {

    suspend fun await(categoryId: Long): Result {
        try {
            categoryRepository.delete(categoryId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            return Result.InternalError(e)
        }

        val orderedIds = categoryRepository.getAll()
            .filterNot(Category::isSystemCategory)
            .map { it.id }

        val defaultCategory = libraryPreferences.defaultCategory.get()
        if (defaultCategory == categoryId.toInt()) {
            libraryPreferences.defaultCategory.delete()
        }

        val categoryPreferences = listOf(
            libraryPreferences.updateCategories,
            libraryPreferences.updateCategoriesExclude,
            downloadPreferences.removeExcludeCategories,
            downloadPreferences.downloadNewChapterCategories,
            downloadPreferences.downloadNewChapterCategoriesExclude,
        )
        val categoryIdString = categoryId.toString()
        categoryPreferences.forEach { preference ->
            val ids = preference.get()
            if (categoryIdString !in ids) return@forEach
            preference.set(ids.minus(categoryIdString))
        }

        return try {
            categoryRepository.updateAllOrders(orderedIds = orderedIds)
            Result.Success
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            Result.InternalError(e)
        }
    }

    sealed interface Result {
        data object Success : Result
        data class InternalError(val error: Throwable) : Result
    }
}
