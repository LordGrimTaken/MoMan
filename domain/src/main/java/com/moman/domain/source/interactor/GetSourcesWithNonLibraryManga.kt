package com.moman.domain.source.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import com.moman.domain.source.model.SourceWithCount
import com.moman.domain.source.repository.SourceRepository

@Inject
class GetSourcesWithNonLibraryManga(
    private val repository: SourceRepository,
) {

    fun subscribe(): Flow<List<SourceWithCount>> {
        return repository.getSourcesWithNonLibraryManga()
    }
}
