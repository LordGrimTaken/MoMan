package com.moman.domain.history.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.history.repository.HistoryRepository

@Inject
class GetTotalReadDuration(
    private val repository: HistoryRepository,
) {

    suspend fun await(): Long {
        return repository.getTotalReadDuration()
    }
}
