package com.moman.domain.track.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.moman.domain.track.model.Track
import com.moman.domain.track.repository.TrackRepository

@Inject
class GetTracksPerManga(
    private val trackRepository: TrackRepository,
) {

    suspend fun await(): Map<Long, List<Track>> {
        return trackRepository.getTracks().groupBy { it.mangaId }
    }

    fun subscribe(): Flow<Map<Long, List<Track>>> {
        return trackRepository.getTracksAsFlow().map { tracks -> tracks.groupBy { it.mangaId } }
    }
}
