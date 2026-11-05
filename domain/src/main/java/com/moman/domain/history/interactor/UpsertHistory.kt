package com.moman.domain.history.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.history.model.HistoryUpdate
import com.moman.domain.history.repository.HistoryRepository

@Inject
class UpsertHistory(
    private val historyRepository: HistoryRepository,
) {

    suspend fun await(historyUpdate: HistoryUpdate) {
        historyRepository.upsertHistory(historyUpdate)
    }
}
