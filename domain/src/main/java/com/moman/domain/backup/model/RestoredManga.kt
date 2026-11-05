package com.moman.domain.backup.model

import com.moman.domain.chapter.model.Chapter
import com.moman.domain.manga.model.Manga
import com.moman.domain.track.model.Track
import kotlin.time.Instant

data class RestoredManga(
    val manga: Manga,
    val chapters: List<Chapter>,
    val categoryIds: List<Long>,
    val history: List<RestoredHistory>,
    val tracks: List<Track>,
    val excludedScanlators: List<String>,
)

data class RestoredHistory(
    val chapterUrl: String,
    val readAt: Instant?,
    val readDuration: Long,
)
