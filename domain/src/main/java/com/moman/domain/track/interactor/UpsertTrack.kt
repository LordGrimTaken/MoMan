package com.moman.domain.track.interactor

import dev.zacsweers.metro.Inject
import logcat.LogPriority
import com.moman.core.common.util.system.logcat
import com.moman.domain.track.model.Track
import com.moman.domain.track.repository.TrackRepository

@Inject
class UpsertTrack(
    private val trackRepository: TrackRepository,
) {

    suspend fun await(track: Track) {
        try {
            trackRepository.upsert(track)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }

    suspend fun awaitAll(tracks: List<Track>) {
        try {
            trackRepository.upsertAll(tracks)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }
}
