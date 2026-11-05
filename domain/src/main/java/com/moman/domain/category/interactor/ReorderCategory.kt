package com.moman.domain.category.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import logcat.LogPriority
import com.moman.core.common.util.system.logcat
import com.moman.domain.category.model.Category
import com.moman.domain.category.repository.CategoryRepository

@Inject
class ReorderCategory(
    private val categoryRepository: CategoryRepository,
) {
    private val mutex = Mutex()

    suspend fun await(category: Category, newIndex: Int) = mutex.withLock {
        val categories = categoryRepository.getAll()
            .filterNot(Category::isSystemCategory)
            .toMutableList()

        val currentIndex = categories.indexOfFirst { it.id == category.id }
        if (currentIndex == -1) {
            return@withLock Result.Unchanged
        }

        try {
            categories.add(newIndex, categories.removeAt(currentIndex))

            categoryRepository.updateAllOrders(orderedIds = categories.map { it.id })
            Result.Success
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            Result.InternalError(e)
        }
    }

    sealed interface Result {
        data object Success : Result
        data object Unchanged : Result
        data class InternalError(val error: Throwable) : Result
    }
}
