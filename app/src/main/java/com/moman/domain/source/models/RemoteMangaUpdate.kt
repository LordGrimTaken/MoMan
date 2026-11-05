package com.moman.domain.source.models

import com.moman.domain.chapter.model.Chapter
import com.moman.domain.manga.model.Manga

data class RemoteMangaUpdate(
    val manga: Manga,
    val newChapters: List<Chapter>,
)
